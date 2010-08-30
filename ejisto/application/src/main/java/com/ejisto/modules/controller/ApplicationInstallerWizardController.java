package com.ejisto.modules.controller;

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
import java.util.ListIterator;

import javax.swing.Action;
import javax.swing.SwingUtilities;

import ch.lambdaj.function.closure.Closure0;
import ch.lambdaj.function.closure.Closure1;

import com.ejisto.modules.controller.wizard.StepController;
import com.ejisto.modules.controller.wizard.StepControllerComparator;
import com.ejisto.modules.controller.wizard.installer.ApplicationScanningController;
import com.ejisto.modules.controller.wizard.installer.ClassesFilteringController;
import com.ejisto.modules.controller.wizard.installer.FileExtractionController;
import com.ejisto.modules.controller.wizard.installer.FileSelectionController;
import com.ejisto.modules.controller.wizard.installer.PropertiesEditingController;
import com.ejisto.modules.controller.wizard.installer.SummaryController;
import com.ejisto.modules.dao.entities.MockedField;
import com.ejisto.modules.gui.components.ApplicationInstallerWizard;
import com.ejisto.modules.gui.components.EjistoDialog;
import com.ejisto.modules.gui.components.helper.CallbackAction;
import com.ejisto.util.WebApplicationDescriptor;

public class ApplicationInstallerWizardController {
    private List<StepController<WebApplicationDescriptor>> controllers;
    private ApplicationInstallerWizard wizard;
    private Closure1<ActionEvent> callActionPerformed;
    private Closure0 closeDialog;
    private Closure0 confirm;
    private Frame application;
    private EjistoDialog dialog;
    private ListIterator<StepController<WebApplicationDescriptor>> stepsIterator;
    private StepController<WebApplicationDescriptor> currentController;
    
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
        stepsIterator=controllers.listIterator();
    }
    
    private void initContainer() {
        
    }
    
    public WebApplicationDescriptor showWizard() {
        initClosures();
        initContainer();
        dialog = new EjistoDialog(application, getMessage("wizard.title"), createWizard(), false);
        initAndSortControllers(dialog);
        dialog.registerAction(new CallbackAction(getMessage("buttons.previous.text"), PREVIOUS_STEP_COMMAND.getValue(), callActionPerformed));
        dialog.registerAction(new CallbackAction(getMessage("buttons.next.text"), NEXT_STEP_COMMAND.getValue(), callActionPerformed));
        Action act = new CallbackAction(getMessage("wizard.ok.text"), CONFIRM.getValue(), confirm);
        act.setEnabled(isSummaryStep());
        dialog.registerAction(act);
        dialog.registerAction(new CallbackAction(getMessage("wizard.close.text"), EjistoDialog.CLOSE_ACTION_COMMAND, closeDialog));
        wizard.initActions();
        dialog.setModalityType(ModalityType.APPLICATION_MODAL);
        dialog.setSize(600, 500);
        centerOnScreen(dialog);
        navigate(true);
        dialog.setVisible(true);
        return currentController.getSession();
    }
    
    private void initClosures() {
        if(callActionPerformed != null) return;
        callActionPerformed = new Closure1<ActionEvent>() {{of(ApplicationInstallerWizardController.this).actionPerformed(var(ActionEvent.class));}};
        closeDialog = new Closure0() {{ of(ApplicationInstallerWizardController.this).closeDialog();}};
        confirm = new Closure0() {{ of(ApplicationInstallerWizardController.this).confirm();}};
    }
    
    void closeDialog() {
        if(showExitWarning()) dialog.close();
    }
    
    synchronized void actionPerformed(ActionEvent e) {
        navigate(e.getActionCommand().equals(NEXT_STEP_COMMAND.getValue()));
    }
    
    private void navigate(final boolean fwd) {
        if((fwd && !stepsIterator.hasNext()) || (!fwd && !stepsIterator.hasPrevious())) {
            if(fwd) confirm();
            return;
        } 
        StepController<WebApplicationDescriptor> controller = fwd ? stepsIterator.next() : stepsIterator.previous();
        if(currentController != null && currentController.equals(controller)) return;
        currentController = controller;
        if(!currentController.canProceed()) return;
        currentController.activate();
        wizard.goToStep(currentController.getStep());
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                executeStep(fwd);
            }
        });
        dialog.getActionFor(PREVIOUS_STEP_COMMAND.getValue()).setEnabled(stepsIterator.hasPrevious());
        dialog.getActionFor(NEXT_STEP_COMMAND.getValue()).setEnabled(!isSummaryStep());
        dialog.getActionFor(CONFIRM.getValue()).setEnabled(isSummaryStep());
    }
    
    
    private void executeStep(boolean fwd) {
        if(currentController.executionCompleted() && fwd && currentController.automaticallyProceedToNextStep()) {
            navigate(true);
        }
    }
    
    private boolean showExitWarning() {
        return showWarning(wizard, "wizard.quit.message");
    }
    
    boolean isSummaryStep() {
        return !stepsIterator.hasNext();
    }
    
    void confirm() {
        dialog.setVisible(false);
    }
    
    private ApplicationInstallerWizard createWizard() {
        wizard = new ApplicationInstallerWizard();
        return wizard;
    }

}
