/*
 * Ejisto, a powerful developer assistant
 *
 * Copyright (C) 2010  Celestino Bellone
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
import com.ejisto.modules.gui.Application;
import org.apache.log4j.Logger;
import org.eclipse.jetty.server.Server;
import org.springframework.context.ApplicationListener;

import javax.annotation.Resource;


public class ServerController implements ApplicationListener<ChangeServerStatus> {

    private static final Logger logger = Logger.getLogger(ServerController.class);

    @Resource
    private Server jettyServer;

    @Resource
    private EventManager eventManager;
    
    @Resource
    private Application application;

    @Override
    public void onApplicationEvent(ChangeServerStatus event) {
        logger.info("handling event: " + event);
        try {
            if (event.getCommand() == ChangeServerStatus.Command.STARTUP) {
                logger.info("Starting Jetty server:");
                jettyServer.start();
                logger.info("done");
            } else {
                logger.info("Stopping Jetty server:");
                jettyServer.stop();
                logger.info("done");
            }
            application.onServerStatusChange(event);
        } catch (Exception e) {
            logger.error("event handling failed", e);
            eventManager.publishEvent(new ApplicationError(this, ApplicationError.Priority.HIGH, e));
        }

    }
}
