/*
 * Ejisto, a powerful developer assistant
 *
 * Copyright (C) 2010-2011  Celestino Bellone
 *
 * Ejisto is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Ejisto is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.ejisto.event.listener;

import com.ejisto.event.EventManager;
import com.ejisto.event.def.ChangeServerStatus;
import com.ejisto.event.def.ContainerInstalled;
import com.ejisto.event.def.InstallContainer;
import com.ejisto.event.def.LogMessage;
import com.ejisto.modules.cargo.CargoManager;
import com.ejisto.modules.conf.SettingsManager;
import com.ejisto.modules.controller.DialogManager;
import com.ejisto.modules.executor.TaskManager;
import com.ejisto.modules.gui.Application;
import com.ejisto.modules.gui.components.ContainerInstallationPanel;
import com.ejisto.util.GuiUtils;
import org.apache.log4j.Logger;
import org.springframework.context.ApplicationListener;

import javax.annotation.Resource;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.concurrent.Callable;

import static com.ejisto.constants.StringConstants.CONTAINERS_HOME_DIR;
import static com.ejisto.modules.executor.TaskManager.createNewGuiTask;
import static com.ejisto.util.GuiUtils.getMessage;
import static com.ejisto.util.GuiUtils.runOnEDT;

/**
 * Created by IntelliJ IDEA.
 * User: celestino
 * Date: 3/6/11
 * Time: 12:46 PM
 */
public class ContainerInstaller implements ApplicationListener<InstallContainer>, PropertyChangeListener {
    private static final Logger logger = Logger.getLogger(ContainerInstaller.class);
    @Resource Application application;
    @Resource CargoManager cargoManager;
    @Resource SettingsManager settingsManager;
    @Resource EventManager eventManager;

    @Override
    public void onApplicationEvent(final InstallContainer event) {
        logger.info("about to install " + event.getDescription() + " container");
        final String containerDescription = settingsManager.getValue("container.default.description");
        final ContainerInstallationPanel panel = new ContainerInstallationPanel(getMessage("container.installation.panel.title"),
                                                                                getMessage("container.installation.panel.description",
                                                                                           containerDescription));
        final DialogManager manager = DialogManager.Builder.newInstance().withContent(panel).withParentFrame(application).withDecorations(
                false).build();

        //new DialogManager(application, panel);
        Callable<Void> action = new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                notifyToPanel(panel, getMessage("container.installation.panel.status.1", containerDescription));
                if (logger.isDebugEnabled()) logger.debug("downloading container");
                cargoManager.downloadAndInstall(settingsManager.getValue("container.default.url"),
                                                System.getProperty(CONTAINERS_HOME_DIR.getValue()));
                if (logger.isDebugEnabled()) logger.debug("download completed");
                notifyToPanel(panel, getMessage("container.installation.panel.status.2", containerDescription));
                if (logger.isDebugEnabled()) logger.debug("notifying installation success");
                eventManager.publishEvent(new ContainerInstalled(this, event.getContainerId(), event.getDescription()));
                eventManager.publishEvent(new LogMessage(this, getMessage("container.installation.ok")));
                if (event.isStart()) eventManager.publishEvent(new ChangeServerStatus(this, ChangeServerStatus.Command.STARTUP));
                showHideProgressPanel(false, manager);
                return null;
            }
        };

        String uuid = TaskManager.getInstance().addNewTask(createNewGuiTask(action, "download server", this));
    }

    private void notifyToPanel(final ContainerInstallationPanel panel, final String message) {
        runOnEDT(new Runnable() {
            @Override
            public void run() {
                panel.notifyJobCompleted(message);
            }
        });
    }

    void showHideProgressPanel(final boolean show, final DialogManager manager) {
        GuiUtils.runOnEDT(new Runnable() {
            @Override
            public void run() {
                if (show) manager.showUndecorated(true);
                else manager.hide();
            }
        });

    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
//        try {
//            evt.getNewValue();
//            //future.get();
//        } catch (InterruptedException e) {
//            logger.error("got interrupted exception", e);
//        } catch (ExecutionException e) {
//            //showHideProgressPanel(false, manager);
//            logger.error("got execution exception", e);
//            eventManager.publishEvent(new ApplicationError(this, ApplicationError.Priority.HIGH, e.getCause()));
//        }
    }
}
