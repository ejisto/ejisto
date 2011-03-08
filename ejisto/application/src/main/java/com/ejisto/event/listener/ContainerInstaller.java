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
import com.ejisto.event.def.InstallContainer;
import com.ejisto.event.def.LogMessage;
import com.ejisto.modules.cargo.CargoManager;
import com.ejisto.modules.conf.SettingsManager;
import com.ejisto.modules.controller.DialogManager;
import com.ejisto.modules.gui.Application;
import com.ejisto.modules.gui.components.ContainerInstallationPanel;
import org.apache.log4j.Logger;
import org.springframework.context.ApplicationListener;

import javax.annotation.Resource;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static com.ejisto.constants.StringConstants.CONTAINERS_HOME_DIR;
import static com.ejisto.util.GuiUtils.getMessage;

/**
 * Created by IntelliJ IDEA.
 * User: celestino
 * Date: 3/6/11
 * Time: 12:46 PM
 */
public class ContainerInstaller implements ApplicationListener<InstallContainer> {
    private static final Logger logger = Logger.getLogger(ContainerInstaller.class);
    @Resource Application application;
    @Resource CargoManager cargoManager;
    @Resource SettingsManager settingsManager;
    @Resource EventManager eventManager;

    @Override
    public void onApplicationEvent(final InstallContainer event) {
        logger.info("about to install " + event.getDescription() + " container");
        final String containerDescription = settingsManager.getValue("container.default.description");
        final ContainerInstallationPanel panel = new ContainerInstallationPanel(
                getMessage("container.installation.panel.title"),
                getMessage("container.installation.panel.description", containerDescription));
        final DialogManager manager = new DialogManager(application, panel);
        Callable<Void> task = new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                panel.notifyJobCompleted(getMessage("container.installation.panel.status.1", containerDescription));
                if (logger.isDebugEnabled()) logger.debug("downloading container");
                cargoManager.downloadAndInstall(settingsManager.getValue("container.default.url"),
                                                System.getProperty(CONTAINERS_HOME_DIR.getValue()));
                if (logger.isDebugEnabled()) logger.debug("download completed");
                panel.notifyJobCompleted(getMessage("container.installation.panel.status.2", containerDescription));
                if (logger.isDebugEnabled()) logger.debug("notifying installation success");
                eventManager.publishEvent(new LogMessage(this, getMessage("container.installation.ok")));
                if (event.isStart())
                    eventManager.publishEvent(new ChangeServerStatus(this, ChangeServerStatus.Command.STARTUP));
                manager.hide();
                return null;
            }
        };
        Future<Void> future = Executors.newSingleThreadExecutor().submit(task);
        manager.show(true);
        try {
            future.get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }

}
