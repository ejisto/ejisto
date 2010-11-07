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

import com.ejisto.modules.controller.WizardException;
import com.ejisto.modules.gui.components.helper.Step;

import javax.swing.*;

/**
 * Defines behavior of specific step controllers
 *
 * @author celestino
 */
public interface StepController<K> {
    /**
     * Returns the view for current step.
     *
     * @return
     */
    JPanel getView();

    /**
     * Validates input params before proceeding to next step
     *
     * @return
     */
    boolean canProceed();

    /**
     * Check if step allows to automatically proceed to next step
     *
     * @return
     */
    boolean automaticallyProceedToNextStep();

    /**
     * Check if step supports "forward"
     *
     * @return
     */
    boolean isForwardEnabled();

    /**
     * Check if step supports "back"
     *
     * @return
     */
    boolean isBackEnabled();

    /**
     * Returns the Controller's exit status
     *
     * @return
     */
    boolean isExecutionSucceeded() throws WizardException;

    /**
     * Returns the Step owned by this controller
     *
     * @return
     */
    Step getStep();

    /**
     * Activates asynchronous jobs and other stuffs, if any
     */
    void activate();

    /**
     * Sets the session object. Useful for data exchange from steps
     *
     * @param session
     */
    void setSession(K session);

    /**
     * Returns the session object
     *
     * @return
     */
    K getSession();

    /**
     * Returns the state of this controller
     */
    boolean executionCompleted();

    /**
     * Validates data inserted by user.
     *
     * @return <code>true</true> if user inserted valid data.
     */
    boolean validateInput();

    /**
     * Do post-execution stuff. Last chance for controllers...
     */
    void beforeNext();

    /**
     * Returns the i18n key for window title, if any
     *
     * @return
     */
    String getTitleKey();

    /**
     * Returns the i18n key for title description, if any
     *
     * @return
     */
    String getDescriptionKey();
}
