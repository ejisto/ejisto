package com.ejisto.modules.controller.wizard.installer;

import com.ejisto.modules.controller.wizard.AbstractStepController;
import com.ejisto.modules.gui.components.EjistoDialog;
import com.ejisto.util.WebApplicationDescriptor;

public abstract class AbstractApplicationInstallerController extends AbstractStepController<WebApplicationDescriptor> {

    public AbstractApplicationInstallerController(EjistoDialog dialog) {
        super(dialog);
    }
    
    @Override
    public boolean automaticallyProceedToNextStep() {
        return false;
    }
    
    @Override
    public void beforeNext() {
    }

}
