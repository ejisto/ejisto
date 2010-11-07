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

package com.ejisto.event;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationEvent;

import javax.swing.*;

public class EventManager implements ApplicationContextAware {
    private ApplicationContext applicationContext;

    public void publishEvent(final ApplicationEvent event) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                publishEventAndWait(event);                
            }
        });
    }
    
    public void publishEventAndWait(ApplicationEvent event) {
        applicationContext.publishEvent(event);                
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
