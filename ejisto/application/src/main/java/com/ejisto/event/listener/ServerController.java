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

import com.ejisto.core.container.ContainerManager;
import com.ejisto.event.EventManager;
import com.ejisto.event.def.ApplicationError;
import com.ejisto.event.def.ChangeServerStatus;
import com.ejisto.event.def.ContainerStatusChanged;
import com.ejisto.event.def.InstallContainer;
import com.ejisto.modules.cargo.NotInstalledException;
import com.ejisto.modules.executor.TaskManager;
import com.ejisto.modules.gui.Application;
import com.ejisto.modules.repository.WebApplicationRepository;
import lombok.extern.log4j.Log4j;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.context.ApplicationListener;

import javax.annotation.Resource;
import java.util.concurrent.Callable;

import static com.ejisto.constants.StringConstants.DEFAULT_CONTAINER_ID;
import static com.ejisto.modules.executor.TaskManager.createNewGuiTask;
import static com.ejisto.util.GuiUtils.runOnEDT;

@Log4j
public class ServerController implements ApplicationListener<ChangeServerStatus>, DisposableBean {

    @Resource(name = "containerManager") private ContainerManager containerManager;
    @Resource private EventManager eventManager;
    @Resource private Application application;
    @Resource private TaskManager taskManager;
    @Resource private WebApplicationRepository webApplicationRepository;

    @Override
    public void onApplicationEvent(final ChangeServerStatus event) {
        log.info("handling event: " + event);
        taskManager.addNewTask(createNewGuiTask(new Callable<Void>() {
            @Override
            public Void call() {
                handleEvent(event);
                return null;
            }
        }, event.getDescription()));
    }

    private void handleEvent(final ChangeServerStatus event) {
        try {
            boolean started = false;
            if (event.getCommand() == ChangeServerStatus.Command.STARTUP) {
                log.info("Starting server:");
                containerManager.startDefault();
                started = true;
                log.info("done");
            } else {
                log.info("Stopping server:");
                containerManager.stopDefault();
                started = false;
                log.info("done");
            }
            handleInstalledWebApplicationsStatus(started);
            runOnEDT(new Runnable() {
                @Override
                public void run() {
                    application.onServerStatusChange(event);
                }
            });

        } catch (NotInstalledException e) {
            log.error("server " + e.getId() + " is not installed.", e);
            eventManager.publishEvent(new InstallContainer(this, e.getId(), true));
        } catch (Exception e) {
            log.error("event handling failed", e);
            eventManager.publishEvent(new ApplicationError(this, ApplicationError.Priority.HIGH, e));
        }
    }

    private void handleInstalledWebApplicationsStatus(boolean started) {
        if (started) webApplicationRepository.containerStartup(DEFAULT_CONTAINER_ID.getValue());
        else webApplicationRepository.containerShutdown(DEFAULT_CONTAINER_ID.getValue());
        ContainerStatusChanged newEvent = new ContainerStatusChanged(this, DEFAULT_CONTAINER_ID.getValue(), started);
        eventManager.publishEvent(newEvent);
    }

    @Override
    public void destroy() throws Exception {
        containerManager.stopDefault();
    }
}
