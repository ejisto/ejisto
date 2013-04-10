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

package com.ejisto.event;

import com.ejisto.event.ApplicationListener;
import com.ejisto.event.def.BaseApplicationEvent;
import com.ejisto.event.def.ShutdownRequest;
import lombok.extern.log4j.Log4j;

import javax.swing.*;
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
public class ApplicationEventDispatcher {
    private final ConcurrentMap<Class<? extends BaseApplicationEvent>, List<ApplicationListener<? extends BaseApplicationEvent>>> registeredListeners;
    private volatile boolean running = true;

    public ApplicationEventDispatcher() {
        registeredListeners = new ConcurrentHashMap<>();
    }

    public <T extends BaseApplicationEvent> void registerApplicationEventListener(ApplicationListener<T> applicationListener) {
        Class<T> eventClass = applicationListener.getTargetEvent();
        registeredListeners.putIfAbsent(eventClass, new ArrayList<ApplicationListener<? extends BaseApplicationEvent>>());
        registeredListeners.get(eventClass).add(applicationListener);
    }

    public <T extends BaseApplicationEvent> void broadcastEvent(final T event) {
        if (!running) {
            return;
        }
        ApplicationEventDispatcher.log.trace("got event of type [" + event.getClass().getName() + "]");
        if (registeredListeners.containsKey(event.getClass())) {
            notifyListeners(event);
        }
        if (ShutdownRequest.class.isInstance(event)) {//poison pill...
            running = false;
        }
    }

    @SuppressWarnings("unchecked")
    private void notifyListeners(final BaseApplicationEvent applicationEvent) {
        final Class<BaseApplicationEvent> eventClass = (Class<BaseApplicationEvent>) applicationEvent.getClass();
        for (final ApplicationListener listener : registeredListeners.get(eventClass)) {
            ApplicationEventDispatcher.log.trace("forwarding event to listener " + listener);
            if (applicationEvent.isRunOnEDT()) {
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        //since registered listener are GUI classes, it is better to
                        //notify them on the Event Dispatch Thread
                        listener.onApplicationEvent(applicationEvent);
                    }
                });
            } else {
                //event that could be handled in a multi threaded context
                listener.onApplicationEvent(applicationEvent);
            }
        }
    }
}
