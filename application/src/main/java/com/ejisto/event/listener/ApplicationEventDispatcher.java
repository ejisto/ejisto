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

import com.ejisto.event.def.BaseApplicationEvent;
import com.ejisto.event.def.ShutdownRequest;
import com.ejisto.util.GuiUtils;
import lombok.extern.log4j.Log4j;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Created by IntelliJ IDEA.
 * User: celestino
 * Date: 8/8/11
 * Time: 9:14 PM
 */
@Log4j
public class ApplicationEventDispatcher implements ApplicationListener<ApplicationEvent> {
    private final ConcurrentMap<Class<?>, List<ApplicationListener<ApplicationEvent>>> registeredListeners;
    private volatile boolean running = true;

    public ApplicationEventDispatcher() {
        registeredListeners = new ConcurrentHashMap<>();
    }

    public void registerApplicationEventListener(Class<?> eventClass, ApplicationListener<ApplicationEvent> applicationListener) {
        registeredListeners.putIfAbsent(eventClass, new ArrayList<ApplicationListener<ApplicationEvent>>());
        registeredListeners.get(eventClass).add(applicationListener);
    }

    @Override
    public void onApplicationEvent(final ApplicationEvent event) {
        if (!running) {
            return;
        }
        log.debug("got event of type [" + event.getClass().getName() + "]");
        if (registeredListeners.containsKey(event.getClass())) {
            notifyListeners(registeredListeners.get(event.getClass()), event);
        }
        if (ShutdownRequest.class.isInstance(event)) {
            running = false;
        }
    }

    private void notifyListeners(List<ApplicationListener<ApplicationEvent>> listeners, final ApplicationEvent applicationEvent) {
        for (final ApplicationListener<ApplicationEvent> listener : listeners) {
            log.debug("forwarding event to listener " + listener);
            if (BaseApplicationEvent.class.isInstance(
                    applicationEvent) && ((BaseApplicationEvent) applicationEvent).isRunOnEDT()) {
                GuiUtils.runOnEDT(new Runnable() {
                    @Override
                    public void run() {
                        //since registered listener are GUI classes, it is better to
                        //notify them on the Event Dispatch Thread
                        listener.onApplicationEvent(applicationEvent);
                    }
                });
            } else {
                //event that could be handled in a multithreaded context
                listener.onApplicationEvent(applicationEvent);
            }

        }
    }
}
