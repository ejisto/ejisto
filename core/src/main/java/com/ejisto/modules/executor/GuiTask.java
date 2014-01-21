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
import lombok.extern.log4j.Log4j;

import javax.swing.*;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicReference;

import static com.ejisto.constants.StringConstants.GUI_TASK_EXCEPTION_PROPERTY;

/**
 * Created by IntelliJ IDEA.
 * User: celestino
 * Date: 11/22/11
 * Time: 7:14 PM
 */
@Log4j
public class GuiTask<T> extends SwingWorker<T, String> implements Task<T> {

    private Callable<T> target;
    private String description;
    private final AtomicReference<ProgressDescriptor> progressDescriptor = new AtomicReference<>();
    private final List<TaskExecutionListener> taskExecutionListeners = new ArrayList<>();
    private String id;

    public GuiTask(Callable<T> target, String description) {
        this.target = target;
        this.description = description;
    }

    protected GuiTask() {

    }

    @Override
    protected final T doInBackground() {
        try {
            return internalDoInBackground();
        } catch (Exception e) {
            firePropertyChange(GUI_TASK_EXCEPTION_PROPERTY.getValue(), null, e);
            throw new ApplicationException(e);
        }
    }

    protected T internalDoInBackground() throws Exception {
        return target.call();
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public boolean supportsProcessChangeNotification() {
        return true;
    }

    @Override
    public void work() {
        execute();
    }

    @Override
    public ProgressDescriptor getCurrentProgressDescriptor() {
        return progressDescriptor.get();
    }

    @Override
    public void addTaskExecutionListener(TaskExecutionListener listener) {
        this.taskExecutionListeners.add(listener);
    }

    @Override
    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    protected void done() {
        taskExecutionListeners.forEach(l -> l.stateChanged(ExecutionState.DONE));
    }

    protected final void notifyJobCompleted(final ProgressDescriptor.ProgressState progressState, final String message) throws InterruptedException, InvocationTargetException {
        SwingUtilities.invokeAndWait(() -> firePropertyChange("progressDescriptor", null, new ProgressDescriptor(0, message, progressState)));
    }

    protected final void notifyJobCompleted(final int progress, final String message) throws InterruptedException, InvocationTargetException {
        SwingUtilities.invokeAndWait(() -> firePropertyChange("progressDescriptor", null, new ProgressDescriptor(progress, message)));
    }

    protected final void addErrorDescriptor(final ErrorDescriptor errorDescriptor) {
        try {
            SwingUtilities.invokeAndWait(() -> firePropertyChange("error", null, errorDescriptor));
        } catch (Exception e) {
            log.error("error during addErrorDescriptor", e);
        }
    }
}
