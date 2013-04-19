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

import com.ejisto.event.ApplicationEventDispatcher;
import com.ejisto.event.def.ApplicationError;
import lombok.extern.log4j.Log4j;

import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;

/**
 * Created by IntelliJ IDEA.
 * User: celestino
 * Date: 11/22/11
 * Time: 7:18 PM
 */
@Log4j
public class BackgroundTask<T> extends FutureTask<T> implements Task<T> {

    private static final ProgressDescriptor NO_PROGRESS = new ProgressDescriptor(0, "");
    private String id;

    public BackgroundTask(Callable<T> callable) {
        super(callable);
    }

    public BackgroundTask(Runnable runnable, T result) {
        super(runnable, result);
    }

    @Override
    public String getDescription() {
        return null;
    }

    @Override
    public boolean supportsProcessChangeNotification() {
        return false;
    }

    @Override
    public void work() {
    }

    @Override
    public ProgressDescriptor getCurrentProgressDescriptor() {
        return NO_PROGRESS;
    }

    @Override
    protected void setException(Throwable t) {
        super.setException(t);
        ApplicationEventDispatcher.publish(new ApplicationError(this, ApplicationError.Priority.HIGH, t));
    }

    @Override
    public void addTaskExecutionListener(TaskExecutionListener listener) {
        throw new UnsupportedOperationException("not implemented.");
    }

    @Override
    public String getId() {
        return this.id;
    }

    @Override
    public void setId(String id) {
        this.id = id;
    }

}
