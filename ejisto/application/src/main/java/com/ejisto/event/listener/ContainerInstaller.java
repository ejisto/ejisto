/*
 * Ejisto, a powerful developer assistant
 *
 * Copyright (C) 2010-2012  Celestino Bellone
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
import com.ejisto.event.def.StatusBarMessage;
import com.ejisto.modules.cargo.CargoManager;
import com.ejisto.modules.cargo.util.DownloadFailed;
import com.ejisto.modules.cargo.util.DownloadTimeout;
import com.ejisto.modules.conf.SettingsManager;
import com.ejisto.modules.controller.DialogController;
import com.ejisto.modules.executor.TaskManager;
import com.ejisto.modules.gui.Application;
import com.ejisto.modules.gui.components.ProgressWithHeader;
import com.ejisto.util.GuiUtils;
import lombok.extern.log4j.Log4j;
import org.springframework.context.ApplicationListener;

import javax.annotation.Resource;
import javax.swing.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.concurrent.Callable;

import static com.ejisto.constants.StringConstants.CONTAINERS_HOME_DIR;
import static com.ejisto.constants.StringConstants.DEFAULT_CONTAINER_DOWNLOAD_URL;
import static com.ejisto.modules.executor.TaskManager.createNewGuiTask;
import static com.ejisto.util.GuiUtils.*;
import static com.ejisto.util.IOUtils.fileToUrl;
import static java.lang.String.format;
import static java.util.Arrays.asList;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

/**
 * Created by IntelliJ IDEA.
 * User: celestino
 * Date: 3/6/11
 * Time: 12:46 PM
 */
@Log4j
public class ContainerInstaller implements ApplicationListener<InstallContainer>, PropertyChangeListener {
    @Resource private Application application;
    @Resource private CargoManager cargoManager;
    @Resource private SettingsManager settingsManager;
    @Resource private EventManager eventManager;

    @Override
    public void onApplicationEvent(final InstallContainer event) {
        log.info("about to install " + event.getDescription() + " container");
        final String containerDescription = settingsManager.getValue("container.default.description");
        final ProgressWithHeader panel = new ProgressWithHeader(
                getMessage("container.installation.panel.title"),
                getMessage("container.installation.panel.description",
                           containerDescription), "container.download.icon");
        final DialogController controller = DialogController.Builder.newInstance().withContent(panel).withParentFrame(
                application).withDecorations(
                false).withIconKey("container.download.icon").build();
        showHideProgressPanel(true, controller);
        Callable<Void> action = new Callable<Void>() {
            @Override
            public Void call() {
                try {
                    notifyToPanel(panel, getMessage("container.installation.panel.status.1", containerDescription));
                    log.debug(format("downloading container from url: %s",
                                     settingsManager.getValue("container.default.url")));
                    boolean success = tryDownload(containerDescription);
                    if (!success) {
                        throw new IllegalStateException("download failed");
                    }
                    log.debug("download completed");
                    notifyToPanel(panel, getMessage("container.installation.panel.status.2", containerDescription));
                    log.debug("notifying installation success");
                    eventManager.publishEvent(
                            new ContainerInstalled(this, event.getContainerId(), event.getDescription()));
                    eventManager.publishEvent(
                            new StatusBarMessage(this, getMessage("container.installation.ok", containerDescription),
                                                 false));
                    if (event.isStart()) {
                        eventManager.publishEvent(new ChangeServerStatus(this, ChangeServerStatus.Command.STARTUP));
                    }
                    return null;
                } finally {
                    showHideProgressPanel(false, controller);
                }
            }
        };

        String uuid = TaskManager.getInstance().addNewTask(createNewGuiTask(action, "download server", this));
        log.debug(String.format("Created download task with uuid %s", uuid));
    }

    private void notifyToPanel(final ProgressWithHeader panel, final String message) {
        runOnEDT(new Runnable() {
            @Override
            public void run() {
                panel.notifyJobCompleted(message);
            }
        });
    }

    private boolean tryDownload(String containerDescription) {
        boolean tryDownload = true;
        String url = settingsManager.getValue("container.default.url");
        while (tryDownload) {
            try {
                log.debug("calling cargoManager");
                cargoManager.downloadAndInstall(url, System.getProperty(CONTAINERS_HOME_DIR.getValue()));
                log.debug("call succeeded");
                settingsManager.putValue(DEFAULT_CONTAINER_DOWNLOAD_URL, url);
                return true;
            } catch (DownloadFailed e) {
                log.debug("got DownloadFailed exception");
                //there was an error while downloading resource
                url = JOptionPane.showInputDialog(null, getMessage("container.download.failed", containerDescription));
                tryDownload = isNotBlank(url);
                if (!tryDownload) {
                    url = selectFileFromDisk(containerDescription, "container.download.canceled");
                    tryDownload = url != null;
                }
            } catch (DownloadTimeout e) {
                log.debug("got DownloadTimeout exception");
                url = selectFileFromDisk(containerDescription, "container.download.timeout");
                tryDownload = url != null;
            } catch (Exception e) {
                log.error("got exception", e);
                throw new IllegalStateException(e);
            }
        }
        return false;
    }

    private String selectFileFromDisk(String containerDescription, String messageKey) {
        JOptionPane.showMessageDialog(null, getMessage(messageKey, containerDescription),
                                      "error",
                                      JOptionPane.ERROR_MESSAGE);
        File localFile = selectFile(null, null, false, asList("zip", "gz", "bz2"));
        if (localFile == null) {
            return null;
        }
        return fileToUrl(localFile).toString();
    }

    void showHideProgressPanel(final boolean show, final DialogController controller) {
        GuiUtils.runOnEDT(new Runnable() {
            @Override
            public void run() {
                if (show) {
                    controller.showUndecorated(true);
                } else {
                    controller.hide();
                }
            }
        });

    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
//        try {
//            evt.getNewValue();
//            //future.get();
//        } catch (InterruptedException e) {
//            log.error("got interrupted exception", e);
//        } catch (ExecutionException e) {
//            //showHideProgressPanel(false, controller);
//            log.error("got execution exception", e);
//            eventManager.publishEvent(new ApplicationError(this, ApplicationError.Priority.HIGH, e.getCause()));
//        }
    }
}
