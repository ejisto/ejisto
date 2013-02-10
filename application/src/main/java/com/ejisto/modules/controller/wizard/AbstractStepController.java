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

package com.ejisto.modules.controller.wizard;

import com.ejisto.core.ApplicationException;
import com.ejisto.modules.executor.ExecutionState;
import com.ejisto.modules.executor.Task;
import com.ejisto.modules.executor.TaskExecutionListener;
import com.ejisto.modules.executor.TaskManager;
import com.ejisto.modules.gui.components.EjistoDialog;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.ejisto.util.GuiUtils.publishError;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

public abstract class AbstractStepController<K> implements StepController<K>, PropertyChangeListener, TaskExecutionListener {
    private final EjistoDialog dialog;
    private K session;
    private final TaskManager taskManager = TaskManager.getInstance();
    private AtomicBoolean done = new AtomicBoolean(false);
    private Semaphore insertPermit = new Semaphore(1);
    private CyclicBarrier barrier = new CyclicBarrier(2, new Runnable() {
        @Override
        public void run() {
            insertPermit.release();
        }
    });
    private String currentTaskUUID;

    protected AbstractStepController(EjistoDialog dialog) {
        this.dialog = dialog;
    }

    public void setSession(K session) {
        this.session = session;
    }

    public K getSession() {
        return session;
    }

    protected EjistoDialog getDialog() {
        return dialog;
    }

    protected <T> String addJob(Task<T> task) {
        if (!insertPermit.tryAcquire()) {
            throw new IllegalStateException("no available permits");
        }
        return taskManager.addNewTask(task);
    }

    @Override
    public void stateChanged(ExecutionState state) {
        switch (state) {
            case DONE:
                waitUntilFinished();
                break;
            default:
                break;

        }
    }

    @Override
    public final void propertyChange(PropertyChangeEvent evt) {
        String propertyName = evt.getPropertyName();
        if (propertyName.equals("exception")) {
            taskManager.cancelTask(currentTaskUUID);
            publishError(this, (Throwable) evt.getNewValue());
            dialog.setVisible(false);//close the dialog
        } else {
            handlePropertyChange(evt);
        }
//        if (evt.getPropertyName().equalsIgnoreCase("state") &&
//                evt.getNewValue() == DONE) {
//            try {
//                done.compareAndSet(false, true);
//                //we should exit as soon as possible, since we are on EDT
//                barrier.await(100, MILLISECONDS);
//            } catch (Exception e) {
//                //should never happens, since this thread should be the second one waiting on barrier.
//                throw new AssertionError(String.format("unexpected %s", e.toString()));
//            }
//        }
    }

    @Override
    public final boolean executionCompleted() {
        Task<?> task = createNewTask();
        if (task != null) {
            task.addTaskExecutionListener(this);
            this.currentTaskUUID = addJob(task);
            try {
                barrier.await();
                return true;
            } catch (Exception e) {
                throw new ApplicationException(e);
            }
        }
        return true;
    }

    protected Task<?> createNewTask() {
        return null;
    }

    protected void handlePropertyChange(PropertyChangeEvent event) {

    }

    protected boolean isDone() {
        return done.get();
    }

    private void waitUntilFinished() {
        try {
            done.compareAndSet(false, true);
            //we should exit as soon as possible, since we are on EDT
            barrier.await(100, MILLISECONDS);
        } catch (Exception e) {
            //should never happens, since this thread should be the second one waiting on barrier.
            throw new AssertionError(String.format("unexpected %s", e.toString()));
        }
    }

}
