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

package com.ejisto.modules.controller;

import static ch.lambdaj.Lambda.forEach;
import static ch.lambdaj.Lambda.var;
import static com.ejisto.constants.StringConstants.CONFIRM;
import static com.ejisto.constants.StringConstants.NEXT_STEP_COMMAND;
import static com.ejisto.constants.StringConstants.PREVIOUS_STEP_COMMAND;
import static com.ejisto.util.GuiUtils.centerOnScreen;
import static com.ejisto.util.GuiUtils.getMessage;
import static com.ejisto.util.GuiUtils.showWarning;
import static java.util.Collections.sort;

import java.awt.Dialog.ModalityType;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Action;
import javax.swing.SwingUtilities;

import org.apache.log4j.Logger;

import ch.lambdaj.function.closure.Closure0;
import ch.lambdaj.function.closure.Closure1;

import com.ejisto.core.jetty.WebApplicationDescriptor;
import com.ejisto.modules.controller.wizard.StepController;
import com.ejisto.modules.controller.wizard.StepControllerComparator;
import com.ejisto.modules.controller.wizard.installer.ApplicationScanningController;
import com.ejisto.modules.controller.wizard.installer.ClassesFilteringController;
import com.ejisto.modules.controller.wizard.installer.FileExtractionController;
import com.ejisto.modules.controller.wizard.installer.FileSelectionController;
import com.ejisto.modules.controller.wizard.installer.PropertiesEditingController;
import com.ejisto.modules.controller.wizard.installer.SummaryController;
import com.ejisto.modules.gui.components.ApplicationInstallerWizard;
import com.ejisto.modules.gui.components.EjistoDialog;
import com.ejisto.modules.gui.components.helper.CallbackAction;

public class ApplicationInstallerWizardController {
	private static final Logger logger = Logger.getLogger(ApplicationInstallerWizard.class);
    private List<StepController<WebApplicationDescriptor>> controllers;
    private ApplicationInstallerWizard wizard;
    private Closure1<ActionEvent> callActionPerformed;
    private Closure0 closeDialog;
    private Closure0 confirm;
    private Frame application;
    private EjistoDialog dialog;
    private int currentIndex = -1;
    private StepController<WebApplicationDescriptor> currentController;
	private boolean success;
    
    public ApplicationInstallerWizardController(Frame application) {
        this.application=application;
    }
    
    private void initAndSortControllers(EjistoDialog dialog) {
        controllers = new ArrayList<StepController<WebApplicationDescriptor>>();
        controllers.add(new FileSelectionController(dialog));
        controllers.add(new FileExtractionController(dialog));
        controllers.add(new ClassesFilteringController(dialog));
        controllers.add(new ApplicationScanningController(dialog));
        controllers.add(new PropertiesEditingController(dialog));
        controllers.add(new SummaryController(dialog));
        sort(controllers, new StepControllerComparator());
        //init session object
        WebApplicationDescriptor session = new WebApplicationDescriptor();
        forEach(controllers).setSession(session);
    }
    
    private void initContainer() {
        for (StepController<WebApplicationDescriptor> controller : controllers) {
			wizard.addTab(controller.getView(), controller.getStep());
		}
    }
    
    public boolean showWizard() {
        initClosures();
        dialog = new EjistoDialog(application, getMessage("wizard.title"), createWizard(), false);
        initAndSortControllers(dialog);
        initContainer();
        dialog.registerAction(new CallbackAction(getMessage("buttons.previous.text"), PREVIOUS_STEP_COMMAND.getValue(), callActionPerformed));
        dialog.registerAction(new CallbackAction(getMessage("buttons.next.text"), NEXT_STEP_COMMAND.getValue(), callActionPerformed));
        Action act = new CallbackAction(getMessage("wizard.ok.text"), CONFIRM.getValue(), confirm);
        act.setEnabled(isSummaryStep());
        dialog.registerAction(act);
        dialog.registerAction(new CallbackAction(getMessage("wizard.close.text"), EjistoDialog.CLOSE_ACTION_COMMAND, closeDialog));
        dialog.setModalityType(ModalityType.APPLICATION_MODAL);
        dialog.setSize(600, 500);
        centerOnScreen(dialog);
        navigate(true);
        dialog.setVisible(true);
        return success;
    }
    
    public WebApplicationDescriptor getWebApplicationDescriptor() {
    	return currentController.getSession();
    }
    
    private void initClosures() {
        if(callActionPerformed != null) return;
        callActionPerformed = new Closure1<ActionEvent>() {{of(ApplicationInstallerWizardController.this).actionPerformed(var(ActionEvent.class));}};
        closeDialog = new Closure0() {{ of(ApplicationInstallerWizardController.this).closeDialog();}};
        confirm = new Closure0() {{ of(ApplicationInstallerWizardController.this).confirm();}};
    }
    
    void closeDialog() {
        if(showExitWarning()) {
        	success=false;
        	dialog.close();
        }
    }
    
    synchronized void actionPerformed(ActionEvent e) {
        navigate(e.getActionCommand().equals(NEXT_STEP_COMMAND.getValue()));
    }
    
    private void navigate(final boolean fwd) {
        if((fwd && !isNextAvailable()) || (!fwd && !isPreviousAvailable())) {
            if(fwd) confirm();
            return;
        } 
        StepController<WebApplicationDescriptor> controller = fwd ? nextController() : previousController();
        if(currentController != null && currentController.equals(controller)) return;
        if(currentController != null) currentController.beforeNext();
        currentController = controller;
        if(!currentController.canProceed()) return;
        currentController.activate();
        wizard.goToStep(currentController.getStep());
        dialog.setHeaderTitle(getMessage(currentController.getTitleKey()));
        dialog.setHeaderDescription(getMessage(currentController.getDescriptionKey()));
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                executeStep(fwd);
            }
        });
        dialog.getActionFor(PREVIOUS_STEP_COMMAND.getValue()).setEnabled(isPreviousAvailable());
        dialog.getActionFor(NEXT_STEP_COMMAND.getValue()).setEnabled(!isSummaryStep());
        dialog.getActionFor(CONFIRM.getValue()).setEnabled(isSummaryStep());
    }
    
    
    private void executeStep(boolean fwd) {
        try {
			if(currentController.executionCompleted() && currentController.isExecutionSucceeded() && fwd && currentController.automaticallyProceedToNextStep()) {
			    navigate(true);
			}
		} catch (WizardException e) {
			logger.error(e.getMessage(),e);
		}
    }
    
    private boolean showExitWarning() {
        return showWarning(wizard, "wizard.quit.message");
    }
    
    boolean isSummaryStep() {
        return !isNextAvailable();
    }
    
    void confirm() {
    	success=true;
        dialog.setVisible(false);
    }
    
    private ApplicationInstallerWizard createWizard() {
        wizard = new ApplicationInstallerWizard();
        return wizard;
    }
    
    private StepController<WebApplicationDescriptor> nextController() {
    	if(!isNextAvailable()) return currentController;
    	return controllers.get(++currentIndex);
    }
    
    private StepController<WebApplicationDescriptor> previousController() {
    	if(!isPreviousAvailable()) return currentController;
    	return controllers.get(--currentIndex);
    }
    
    private boolean isNextAvailable() {
    	return currentIndex + 1 < controllers.size();
    }
    
    private boolean isPreviousAvailable() {
    	return currentIndex - 1 >= 0;
    }

}
