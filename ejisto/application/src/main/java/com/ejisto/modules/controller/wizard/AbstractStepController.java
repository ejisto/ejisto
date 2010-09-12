/*******************************************************************************
 * Copyright 2010 Celestino Bellone
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package com.ejisto.modules.controller.wizard;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import com.ejisto.modules.gui.components.EjistoDialog;

public abstract class AbstractStepController<K> implements StepController<K> {
    private EjistoDialog dialog;
    private K session;
    private ExecutorService executor;

    public AbstractStepController(EjistoDialog dialog) {
        this.dialog=dialog;
        this.executor = Executors.newCachedThreadPool();
    }
    
    public void setSession(K session) {
        this.session=session;
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
