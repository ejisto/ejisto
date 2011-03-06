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
import com.ejisto.event.def.ApplicationError;
import com.ejisto.event.def.ChangeServerStatus;
import com.ejisto.event.def.InstallContainer;
import com.ejisto.event.def.LogMessage;
import com.ejisto.modules.cargo.CargoManager;
import com.ejisto.modules.conf.SettingsManager;
import com.ejisto.modules.controller.DialogManager;
import com.ejisto.modules.gui.Application;
import com.ejisto.modules.gui.components.ContainerInstallationPanel;
import org.springframework.context.ApplicationListener;

import javax.annotation.Resource;

import static com.ejisto.constants.StringConstants.CONTAINERS_HOME_DIR;
import static com.ejisto.util.GuiUtils.getMessage;

/**
 * Created by IntelliJ IDEA.
 * User: celestino
 * Date: 3/6/11
 * Time: 12:46 PM
 */
public class ContainerInstaller implements ApplicationListener<InstallContainer> {
    @Resource Application application;
    @Resource CargoManager cargoManager;
    @Resource SettingsManager settingsManager;
    @Resource EventManager eventManager;

    @Override
    public void onApplicationEvent(InstallContainer event) {
        String containerDescription = settingsManager.getValue("container.default.description");
        ContainerInstallationPanel panel = new ContainerInstallationPanel(
                getMessage("container.installation.panel.title"),
                getMessage("container.installation.panel.description", containerDescription));
        DialogManager manager = new DialogManager(application, panel);
        manager.showInSeparateThread(true);
        try {
            panel.notifyJobCompleted(getMessage("container.installation.panel.status.1", containerDescription));
            cargoManager.downloadAndInstall(settingsManager.getValue("container.default.url"),
                                            settingsManager.getValue(CONTAINERS_HOME_DIR));
            panel.notifyJobCompleted(getMessage("container.installation.panel.status.2", containerDescription));
            eventManager.publishEvent(new LogMessage(this, getMessage("container.installation.ok")));
            if (event.isStart())
                eventManager.publishEvent(new ChangeServerStatus(this, ChangeServerStatus.Command.STARTUP));
        } catch (Exception e) {
            manager.hide();
            eventManager.publishEvent(new ApplicationError(this, ApplicationError.Priority.HIGH, e));
        }
    }
}
