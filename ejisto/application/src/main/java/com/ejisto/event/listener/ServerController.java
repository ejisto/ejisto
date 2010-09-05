/*
 * Copyright 2010 Celestino Bellone
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions
 * and limitations under the License.
 */

package com.ejisto.event.listener;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.eclipse.jetty.server.Server;
import org.springframework.context.ApplicationListener;

import com.ejisto.event.EventManager;
import com.ejisto.event.def.ApplicationError;
import com.ejisto.event.def.ChangeServerStatus;
import com.ejisto.modules.gui.Application;


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
