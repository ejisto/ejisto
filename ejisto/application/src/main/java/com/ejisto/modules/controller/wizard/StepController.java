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
    
    /**
     * Returns the i18n key for window title, if any
     * @return
     */
    String getTitleKey();
    
    /**
     * Returns the i18n key for title description, if any
     * @return
     */
    String getDescriptionKey();
}
