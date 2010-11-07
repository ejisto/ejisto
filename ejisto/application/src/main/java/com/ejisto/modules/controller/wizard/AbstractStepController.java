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

package com.ejisto.modules.controller.wizard;

import com.ejisto.modules.gui.components.EjistoDialog;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public abstract class AbstractStepController<K> implements StepController<K> {
    private EjistoDialog dialog;
    private K session;
    private ExecutorService executor;

    public AbstractStepController(EjistoDialog dialog) {
        this.dialog = dialog;
        this.executor = Executors.newCachedThreadPool();
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

    protected Future<?> addJob(Runnable task) {
        return executor.submit(task);
    }

    protected <T> Future<T> addJob(Callable<T> task) {
        return executor.submit(task);
    }

    protected void execute(Runnable task) {
        executor.execute(task);
    }

}
