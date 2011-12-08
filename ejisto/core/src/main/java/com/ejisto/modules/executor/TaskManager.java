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

package com.ejisto.modules.executor;

import ch.lambdaj.Lambda;
import org.springframework.beans.factory.DisposableBean;

import java.beans.PropertyChangeListener;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.locks.ReentrantLock;

import static ch.lambdaj.Lambda.forEach;
import static java.util.Collections.emptyList;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.springframework.util.CollectionUtils.isEmpty;

/**
 * Created by IntelliJ IDEA.
 * User: celestino
 * Date: 4/2/11
 * Time: 6:48 PM
 */
public class TaskManager implements DisposableBean {
    private static final TaskManager INSTANCE = new TaskManager();
    private ExecutorService executorService;
    private ScheduledExecutorService scheduler;
    private final ReentrantLock lock = new ReentrantLock();
    private final Map<String, TaskEntry> registry;
    private final Semaphore semaphore;

    public static TaskManager getInstance() {
        return INSTANCE;
    }

    private TaskManager() {
        this.semaphore = new Semaphore(Runtime.getRuntime().availableProcessors() + 2);
        this.registry = Collections.synchronizedMap(new LinkedHashMap<String, TaskEntry>());
        this.executorService = Executors.newCachedThreadPool();
        this.scheduler = Executors.newScheduledThreadPool(5);
        this.scheduler.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                refreshTasksList();
            }
        }, 500L, 500L, MILLISECONDS);
    }

    private void refreshTasksList() {
        if (!lock.tryLock()) return;
        try {
            List<String> toBeRemoved = new ArrayList<String>();
            Future<?> future;
            TaskEntry entry;
            for (String key : registry.keySet()) {
                entry = registry.get(key);
                future = entry.getFuture();
                if (future.isCancelled() || future.isDone()) toBeRemoved.add(key);

            }
            for (String key : toBeRemoved) registry.remove(key);
        } finally {
            lock.unlock();
        }

    }

    public String addNewTask(Task<?> task) {
        boolean locked = false;
        try {
            locked = lock.tryLock(1, SECONDS);
            if (!locked) return null;
            String uuid = UUID.randomUUID().toString();
            Future<?> future;
            if (task.supportsProcessChangeNotification()) future = task;
            else future = internalAddTask(task, uuid);
            task.work();
            registerTask(uuid, task, future);
            return uuid;
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            if (locked) lock.unlock();
        }
    }

    private Future<?> internalAddTask(Task<?> task, String uuid) {
        return executorService.submit(task);
    }

    private void registerTask(String uuid, Task<?> task, Future<?> future) {
        registry.put(uuid, new TaskEntry(future, buildTaskDescriptor(uuid, task, future), task));
    }


    public void scheduleTaskAtFixedRate(Runnable task, long delay, long period, TimeUnit timeUnit) {
        scheduler.scheduleAtFixedRate(task, delay, period, timeUnit);
    }

    public void cancelTask(String uuid) {
        if (!lock.tryLock()) return;
        try {
            Future<?> future = registry.get(uuid).getFuture();
            if (future != null) future.cancel(true);
        } finally {
            lock.unlock();
        }
    }

    public List<TaskDescriptor> getRegisteredTasks() {
        if (isEmpty(registry)) return emptyList();
        return new ArrayList<TaskDescriptor>(
                Lambda.<TaskDescriptor>collect(forEach(registry.values()).getDescriptor()));
    }

    private TaskDescriptor buildTaskDescriptor(String uuid, Task task, Future<?> future) {
        return new TaskDescriptor(uuid, task.getDescription(), getExecutionState(future));
    }

    private ExecutionState getExecutionState(Future<?> future) {
        if (future.isCancelled()) return ExecutionState.CANCELED;
        if (future.isDone()) return ExecutionState.DONE;
        return ExecutionState.RUNNING;
    }

    private void shutdownExecutorService(ExecutorService service) {
        try {
            service.shutdown();
            if (!service.awaitTermination(5L, SECONDS)) service.shutdownNow();
        } catch (InterruptedException e) {
            service.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public void destroy() throws Exception {
        shutdownExecutorService(this.executorService);
        shutdownExecutorService(this.scheduler);
    }

    private class TaskEntry {
        private final Future<?> future;
        private final TaskDescriptor descriptor;
        private final Task<?> task;

        private TaskEntry(Future<?> future, TaskDescriptor descriptor, Task<?> task) {
            this.future = future;
            this.descriptor = descriptor;
            this.task = task;
        }

        public Future<?> getFuture() {
            return future;
        }

        public TaskDescriptor getDescriptor() {
            return descriptor;
        }

        public Task<?> getTask() {
            return task;
        }
    }

    public static <T> GuiTask<T> createNewGuiTask(Callable<T> callable, String description, PropertyChangeListener... listeners) {
        GuiTask<T> task = new GuiTask<T>(callable, description);
        for (PropertyChangeListener listener : listeners) {
            task.addPropertyChangeListener(listener);
        }
        return task;
    }

    public static <T> BackgroundTask<T> createNewBackgroundTask(Callable<T> callable, String description) {
        return new BackgroundTask<T>(callable);
    }

}
