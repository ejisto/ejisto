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

package com.ejisto.event;

import com.ejisto.modules.executor.TaskManager;
import org.apache.log4j.Logger;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationEvent;

import javax.annotation.Resource;

public class EventManager implements ApplicationContextAware {
    private static final Logger logger = Logger.getLogger(EventManager.class);
    private boolean initialized = false;
    private ApplicationContext applicationContext;
    @Resource private TaskManager taskManager;

    public void publishEvent(final ApplicationEvent event) {
        if (!initialized) {
            logger.warn("discarded event from " + event.getSource() + " " + event);
            return;
        }
        taskManager.addTask(new Runnable() {
            @Override
            public void run() {
                publishEventAndWait(event);
            }
        }, event.toString());
    }

    public void publishEventAndWait(ApplicationEvent event) {
        applicationContext.publishEvent(event);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
        this.initialized = true;
    }
}
