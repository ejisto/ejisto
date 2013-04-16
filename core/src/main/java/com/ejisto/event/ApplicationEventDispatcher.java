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

import com.ejisto.event.def.BaseApplicationEvent;
import com.ejisto.event.def.ShutdownRequest;
import com.ejisto.modules.executor.BackgroundTask;
import com.ejisto.modules.executor.TaskManager;
import lombok.extern.log4j.Log4j;
import org.apache.commons.collections.CollectionUtils;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

/**
 * Created by IntelliJ IDEA.
 * User: celestino
 * Date: 8/8/11
 * Time: 9:14 PM
 */
@Log4j
public class ApplicationEventDispatcher {
    private static final BlockingQueue<BaseApplicationEvent> EVENT_QUEUE = new LinkedBlockingQueue<>();
    private final ConcurrentMap<Class<? extends BaseApplicationEvent>, List<ApplicationListener<? extends BaseApplicationEvent>>> registeredListeners;
    private volatile boolean running = false;
    private final TaskManager taskManager;

    public ApplicationEventDispatcher(TaskManager taskManager) {
        this.registeredListeners = new ConcurrentHashMap<>();
        this.taskManager = taskManager;
        this.running = true;
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    processPendingEvents();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new IllegalStateException(e);
                }
            }
        }, "applicationEventDispatcher");
        t.setDaemon(true);
        t.start();
    }

    public <T extends BaseApplicationEvent> void registerApplicationEventListener(ApplicationListener<T> applicationListener) {
        Class<T> eventClass = applicationListener.getTargetEventType();
        registeredListeners.putIfAbsent(eventClass,
                                        new ArrayList<ApplicationListener<? extends BaseApplicationEvent>>());
        registeredListeners.get(eventClass).add(applicationListener);
    }

    public <T extends BaseApplicationEvent> void broadcast(final T event) {
        if (!running) {
            return;
        }
        log.trace("got event of type [" + event.getClass().getName() + "]");
        taskManager.addNewTask(new BackgroundTask<Void>(new Runnable() {
            @Override
            public void run() {
                notifyListeners(event);
            }
        }, null));
        if (ShutdownRequest.class.isInstance(event)) {//poison pill
            running = false;
        }
    }

    public static void publish(BaseApplicationEvent event) {
        EVENT_QUEUE.add(event);
    }

    private void processPendingEvents() throws InterruptedException {
        while (running) {
            broadcast(EVENT_QUEUE.take());
        }
        EVENT_QUEUE.clear();
    }

    @SuppressWarnings("unchecked")
    private void notifyListeners(final BaseApplicationEvent applicationEvent) {
        final Class<BaseApplicationEvent> eventClass = (Class<BaseApplicationEvent>) applicationEvent.getClass();
        log.trace("got event of type: " + eventClass.getName());
        List<ApplicationListener<? extends BaseApplicationEvent>> listeners = registeredListeners.get(eventClass);
        if (CollectionUtils.isEmpty(listeners)) {
            return;
        }
        for (final ApplicationListener listener : listeners) {
            log.trace("forwarding event to listener " + listener);
            if (applicationEvent.shouldRunOnEDT()) {
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
