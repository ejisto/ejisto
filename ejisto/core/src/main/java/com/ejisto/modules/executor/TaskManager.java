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

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import static ch.lambdaj.Lambda.collect;
import static ch.lambdaj.Lambda.forEach;
import static java.util.Collections.emptyList;
import static org.springframework.util.CollectionUtils.isEmpty;

/**
 * Created by IntelliJ IDEA.
 * User: celestino
 * Date: 4/2/11
 * Time: 6:48 PM
 */
public class TaskManager {
    private static final TaskManager INSTANCE = new TaskManager();
    private ExecutorService executorService;
    private final ReentrantLock lock = new ReentrantLock();
    private final Map<String, TaskEntry> registry;

    public static TaskManager getInstance() {
        return INSTANCE;
    }

    private TaskManager() {
        this.registry = Collections.synchronizedMap(new LinkedHashMap<String, TaskEntry>());
        this.executorService = Executors.newCachedThreadPool();
        TimerTask refreshTask = new TimerTask() {
            @Override
            public void run() {
                refreshTasksList();
            }
        };
        new Timer("refreshTask").scheduleAtFixedRate(refreshTask, 500L, 500L);
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

    public <T> Future<T> addTask(Task<T> task) {
        boolean locked = false;
        try {
            locked = lock.tryLock(1, TimeUnit.SECONDS);
            if (!locked) return null;
            Future<T> future = executorService.submit(task);
            String uuid = UUID.randomUUID().toString();
            registry.put(uuid, new TaskEntry(future, buildTaskDescriptor(uuid, task, future), task));
            return future;
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            if (locked) lock.unlock();
        }
    }

    public Future<Void> addTask(Runnable target, String description) {
        return addTask(new Task<Void>(Executors.callable(target, (Void) null), description));
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

    @SuppressWarnings("unchecked")
    public List<TaskDescriptor> getRegisteredTasks() {
        if (isEmpty(registry)) return emptyList();
        return (List<TaskDescriptor>) collect(forEach(registry.values()).getDescriptor());
    }

    private TaskDescriptor buildTaskDescriptor(String uuid, Task task, Future<?> future) {
        return new TaskDescriptor(uuid, task.getDescription(), getExecutionState(future));
    }

    private ExecutionState getExecutionState(Future<?> future) {
        if (future.isCancelled()) return ExecutionState.CANCELED;
        if (future.isDone()) return ExecutionState.DONE;
        return ExecutionState.RUNNING;
    }

    private class TaskEntry {
        private Future<?> future;
        private TaskDescriptor descriptor;
        private Task task;

        private TaskEntry(Future<?> future, TaskDescriptor descriptor, Task task) {
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

        public Task getTask() {
            return task;
        }
    }

}
