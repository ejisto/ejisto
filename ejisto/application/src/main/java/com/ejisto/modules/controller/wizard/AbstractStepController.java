package com.ejisto.modules.controller.wizard;

import com.ejisto.modules.gui.components.EjistoDialog;

public abstract class AbstractStepController<K> implements StepController<K> {
    private EjistoDialog dialog;
    private K session;

    public AbstractStepController(EjistoDialog dialog) {
        this.dialog=dialog;
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
    
}
