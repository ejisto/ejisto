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

import ch.lambdaj.function.closure.Closure;
import com.ejisto.core.container.ContainerManager;
import com.ejisto.event.EventManager;
import com.ejisto.event.def.ApplicationError;
import com.ejisto.event.def.ChangeServerStatus;
import com.ejisto.event.def.InstallContainer;
import com.ejisto.modules.cargo.NotInstalledException;
import com.ejisto.modules.executor.TaskManager;
import com.ejisto.modules.gui.Application;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.context.ApplicationListener;

import javax.annotation.Resource;

import static ch.lambdaj.Lambda.closure;
import static ch.lambdaj.Lambda.of;
import static com.ejisto.util.GuiUtils.runInEDT;

public class ServerController implements ApplicationListener<ChangeServerStatus>, DisposableBean {

    private static final Logger logger = Logger.getLogger(ServerController.class);

    @Resource(name = "containerManager") private ContainerManager containerManager;
    @Resource private EventManager eventManager;
    @Resource private Application application;
    @Resource private TaskManager taskManager;

    @Override
    public void onApplicationEvent(final ChangeServerStatus event) {
        logger.info("handling event: " + event);
        taskManager.addTask(new Runnable() {
            @Override
            public void run() {
                handleEvent(event);
            }
        }, event.getDescription());
    }

    private void handleEvent(ChangeServerStatus event) {
        try {
            if (event.getCommand() == ChangeServerStatus.Command.STARTUP) {
                logger.info("Starting server:");
                containerManager.startDefault();
                logger.info("done");
            } else {
                logger.info("Stopping server:");
                containerManager.stopDefault();
                logger.info("done");
            }
            Closure c = closure();
            {of(application).onServerStatusChange(event);}
            runInEDT(c);

        } catch (NotInstalledException e) {
            logger.error("server " + e.getId() + " is not installed.", e);
            eventManager.publishEvent(new InstallContainer(this, e.getId(), true));
        } catch (Exception e) {
            logger.error("event handling failed", e);
            eventManager.publishEvent(new ApplicationError(this, ApplicationError.Priority.HIGH, e));
        }
    }

    @Override
    public void destroy() throws Exception {
        containerManager.stopDefault();
    }
}
