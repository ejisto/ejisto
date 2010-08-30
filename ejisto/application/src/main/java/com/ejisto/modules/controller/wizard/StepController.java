package com.ejisto.modules.controller.wizard;

import javax.swing.JPanel;

import com.ejisto.modules.controller.WizardException;
import com.ejisto.modules.gui.components.helper.Step;

/**
 * Defines behavior of specific step controllers
 * @author celestino
 *
 */
public interface StepController<K> {
    /**
     * Returns the view for current step.
     * @return
     */
    JPanel getView();
    
    /**
     * Validates input params before proceeding to next step
     * @return
     */
    boolean canProceed();
    
    /**
     * Check if step allows to automatically proceed to next step
     * @return
     */
    boolean automaticallyProceedToNextStep();
    
    /**
     * Returns the Controller's exit status
     * @return
     */
    boolean isExecutionSucceeded() throws WizardException;
    
    /**
     * Returns the Step owned by this controller
     * @return
     */
    Step getStep();
    
    /**
     * Activates asynchronous jobs and other stuffs, if any
     */
    void activate();
    
    /**
     * Sets the session object. Useful for data exchange from steps
     * @param session
     */
    void setSession(K session);
    
    /**
     * Returns the session object
     * @return
     */
    K getSession();
    
    /**
     * Returns the state of this controller
     */
    boolean executionCompleted();
    
    /**
     * Do post-execution stuff. Last chance for controllers...
     */
    void beforeNext();
}
