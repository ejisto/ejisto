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

package com.ejisto.modules.executor;

import com.ejisto.core.ApplicationException;

import java.beans.PropertyChangeListener;
import java.lang.ref.SoftReference;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

import static java.util.Collections.emptyList;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.collections.MapUtils.isEmpty;

/**
 * Created by IntelliJ IDEA.
 * User: celestino
 * Date: 4/2/11
 * Time: 6:48 PM
 */
public final class TaskManager {

    private ExecutorService executorService;
    private ScheduledExecutorService scheduler;
    private final ReentrantLock lock = new ReentrantLock();
    private final ConcurrentMap<String, TaskEntry> registry;

    public TaskManager() {
        this.registry = new ConcurrentHashMap<>();
        this.executorService = Executors.newCachedThreadPool();
        this.scheduler = Executors.newScheduledThreadPool(5);
        this.scheduler.scheduleAtFixedRate(this::refreshTasksList, 500L, 500L, MILLISECONDS);
    }

    private void refreshTasksList() {
        if (!lock.tryLock()) {
            return;
        }
        try {
            registry.entrySet()
                    .stream()
                    .filter(e -> {
                        Future<?> future = e.getValue().getFuture();
                        return future.isCancelled() || future.isDone();
                    }).collect(toList())
                    .forEach(e -> registry.remove(e.getKey(), e.getValue()));
        } finally {
            lock.unlock();
        }

    }

    public String addNewTask(Task<?> task) {
        boolean locked = false;
        try {
            locked = lock.tryLock(1, SECONDS);
            if (!locked) {
                return null;
            }
            String uuid = UUID.randomUUID().toString();
            Future<?> future;
            if (task.supportsProcessChangeNotification()) {
                future = task;
            } else {
                future = internalAddTask(task);
            }
            task.work();
            registerTask(uuid, task, future);
            return uuid;
        } catch (Exception e) {
            throw new ApplicationException(e);
        } finally {
            if (locked) {
                lock.unlock();
            }
        }
    }

    private Future<?> internalAddTask(Task<?> task) {
        return executorService.submit(task);
    }

    private void registerTask(String uuid, Task<?> task, Future<?> future) {
        registry.put(uuid, new TaskEntry(future, buildTaskDescriptor(uuid, task, future), task));
    }


    public void scheduleTaskAtFixedRate(Runnable task, long delay, long period, TimeUnit timeUnit) {
        scheduler.scheduleAtFixedRate(task, delay, period, timeUnit);
    }

    public void cancelTask(String uuid) {
        if (!lock.tryLock()) {
            return;
        }
        try {
            TaskEntry entry = registry.get(uuid);
            if (entry == null) {
                return;
            }
            Future<?> future = entry.getFuture();
            if (future != null) {
                future.cancel(true);
            }
        } finally {
            lock.unlock();
        }
    }

    public List<TaskDescriptor> getRegisteredTasks() {
        if (isEmpty(registry)) {
            return emptyList();
        }
        return registry.values().stream().map(TaskEntry::getDescriptor).collect(toList());
    }

    private TaskDescriptor buildTaskDescriptor(String uuid, Task task, Future<?> future) {
        return new TaskDescriptor(uuid, task.getDescription(), getExecutionState(future));
    }

    private ExecutionState getExecutionState(Future<?> future) {
        if (future.isCancelled()) {
            return ExecutionState.CANCELED;
        }
        if (future.isDone()) {
            return ExecutionState.DONE;
        }
        return ExecutionState.RUNNING;
    }

    private void shutdownExecutorService(ExecutorService service) {
        try {
            service.shutdown();
            if (!service.awaitTermination(5L, SECONDS)) {
                service.shutdownNow();
            }
        } catch (InterruptedException e) {
            service.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    private final class TaskEntry {
        private final SoftReference<Future<?>> future;
        private final TaskDescriptor descriptor;
        private final SoftReference<Task<?>> task;

        private TaskEntry(Future<?> future, TaskDescriptor descriptor, Task<?> task) {
            this.future = new SoftReference<>(future);
            this.descriptor = descriptor;
            this.task = new SoftReference<>(task);
        }

        public Future<?> getFuture() {
            return future.get();
        }

        public TaskDescriptor getDescriptor() {
            return descriptor;
        }

        public Task<?> getTask() {
            return task.get();
        }
    }

    public static <T> GuiTask<T> createNewGuiTask(Callable<T> callable, String description, PropertyChangeListener... listeners) {
        GuiTask<T> task = new GuiTask<>(callable, description);
        Arrays.stream(listeners).forEach(task::addPropertyChangeListener);
        return task;
    }

    public static <T> BackgroundTask<T> createNewBackgroundTask(Callable<T> callable, String description) {
        return new BackgroundTask<>(callable);
    }

    public static final class Descriptor {
        private final Runnable task;
        private final long initialDelay;
        private final long period;

        public Descriptor(Runnable task, long initialDelay, long period) {
            this.task = task;
            this.initialDelay = initialDelay;
            this.period = period;
        }

        public Runnable getTask() {
            return task;
        }

        public long getInitialDelay() {
            return initialDelay;
        }

        public long getPeriod() {
            return period;
        }
    }

}
