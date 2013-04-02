/*
 * Ejisto, a powerful developer assistant
 *
 * Copyright (C) 2010-2013 Celestino Bellone
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
import com.ejisto.modules.repository.ContainersRepository;
import com.ejisto.modules.repository.WebApplicationRepository;
import lombok.extern.log4j.Log4j;
import org.springframework.context.ApplicationListener;

import java.util.concurrent.Callable;

import static com.ejisto.constants.StringConstants.DEFAULT_CONTAINER_ID;
import static com.ejisto.modules.executor.TaskManager.createNewGuiTask;
import static com.ejisto.util.GuiUtils.runOnEDT;
import static java.lang.String.format;

@Log4j
public class ServerController implements ApplicationListener<ChangeServerStatus> {

    private final ContainerManager containerManager;
    private final EventManager eventManager;
    private final Application application;
    private final TaskManager taskManager;
    private final WebApplicationRepository webApplicationRepository;
    private final ContainersRepository containersRepository;

    public ServerController(ContainerManager containerManager,
                            EventManager eventManager,
                            Application application,
                            TaskManager taskManager,
                            WebApplicationRepository webApplicationRepository,
                            ContainersRepository containersRepository) {
        this.containerManager = containerManager;
        this.eventManager = eventManager;
        this.application = application;
        this.taskManager = taskManager;
        this.webApplicationRepository = webApplicationRepository;
        this.containersRepository = containersRepository;
    }

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
            boolean started;
            String containerId = event.getContainerId();
            if (event.getCommand() == ChangeServerStatus.Command.STARTUP) {
                log.info(format("Starting server %s:", containerId));
                containerManager.start(containersRepository.loadContainer(event.getContainerId()));
                started = true;
                log.info("done");
            } else {
                log.info(format("Stopping server %s:", containerId));
                containerManager.stop(containersRepository.loadContainer(containerId));
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
        if (started) {
            webApplicationRepository.containerStartup(DEFAULT_CONTAINER_ID.getValue());
        } else {
            webApplicationRepository.containerShutdown(DEFAULT_CONTAINER_ID.getValue());
        }
        ContainerStatusChanged newEvent = new ContainerStatusChanged(this, DEFAULT_CONTAINER_ID.getValue(), started);
        eventManager.publishEvent(newEvent);
    }
}
