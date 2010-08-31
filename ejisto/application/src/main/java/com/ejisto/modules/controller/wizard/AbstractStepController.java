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
