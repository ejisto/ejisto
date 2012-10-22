/*
 * Ejisto, a powerful developer assistant
 *
 * Copyright (C) 2010-2012  Celestino Bellone
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

package com.ejisto.modules.controller;

import ch.lambdaj.function.closure.Closure0;
import ch.lambdaj.function.closure.Closure1;
import com.ejisto.modules.controller.wizard.StepController;
import com.ejisto.modules.controller.wizard.StepControllerComparator;
import com.ejisto.modules.controller.wizard.installer.*;
import com.ejisto.modules.dao.entities.WebApplicationDescriptor;
import com.ejisto.modules.executor.TaskManager;
import com.ejisto.modules.gui.components.ApplicationInstallerWizard;
import com.ejisto.modules.gui.components.EjistoDialog;
import com.ejisto.modules.gui.components.helper.CallbackAction;
import lombok.extern.log4j.Log4j;

import javax.swing.*;
import java.awt.Dialog.ModalityType;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;

import static ch.lambdaj.Lambda.forEach;
import static ch.lambdaj.Lambda.var;
import static com.ejisto.constants.StringConstants.*;
import static com.ejisto.modules.executor.TaskManager.createNewGuiTask;
import static com.ejisto.util.GuiUtils.*;
import static java.util.Collections.sort;

@Log4j
public class ApplicationInstallerWizardController implements PropertyChangeListener {
    private List<StepController<WebApplicationDescriptor>> controllers;
    private ApplicationInstallerWizard wizard;
    private Closure1<ActionEvent> callActionPerformed;
    private Closure0 closeDialog;
    private Closure0 confirm;
    private final Frame application;
    private EjistoDialog dialog;
    private AtomicInteger currentIndex = new AtomicInteger(-1);
    private StepController<WebApplicationDescriptor> currentController;
    private boolean success;
    private final String containerHome;

    public ApplicationInstallerWizardController(Frame application, String containerHome) {
        this.application = application;
        this.containerHome = containerHome;
    }

    private void initAndSortControllers(EjistoDialog dialog) {
        controllers = new ArrayList<>();
        controllers.add(new FileSelectionController(dialog));
        controllers.add(new FileExtractionController(dialog));
        controllers.add(new ClassesFilteringController(dialog));
        controllers.add(new ApplicationScanningController(dialog, containerHome));
        controllers.add(new JndiResourcesEditorController(dialog));
        controllers.add(new PropertiesEditingController(dialog));
        controllers.add(new SummaryController(dialog));
        sort(controllers, new StepControllerComparator());
        //setTypes session object
        WebApplicationDescriptor session = new WebApplicationDescriptor();
        session.setContainerId(DEFAULT_CONTAINER_ID.getValue());
        forEach(controllers).setSession(session);
    }

    private void initContainer() {
        for (StepController<WebApplicationDescriptor> controller : controllers) {
            wizard.addTab(controller.getView(), controller.getStep());
        }
    }

    public boolean showWizard() {
        initClosures();
        dialog = new EjistoDialog(application, getMessage("wizard.title"), createWizard(), false,
                                  "application.installer.icon");
        initAndSortControllers(dialog);
        initContainer();
        dialog.registerAction(new CallbackAction(getMessage("buttons.previous.text"), PREVIOUS_STEP_COMMAND.getValue(),
                                                 callActionPerformed));
        dialog.registerAction(
                new CallbackAction(getMessage("buttons.next.text"), NEXT_STEP_COMMAND.getValue(), callActionPerformed));
        Action act = new CallbackAction(getMessage("wizard.ok.text"), CONFIRM.getValue(), confirm);
        act.setEnabled(isSummaryStep());
        dialog.registerAction(act);
        dialog.registerAction(
                new CallbackAction(getMessage("wizard.close.text"), EjistoDialog.CLOSE_ACTION_COMMAND, closeDialog));
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
        if (callActionPerformed != null) {
            return;
        }
        callActionPerformed = new Closure1<ActionEvent>() {{
            of(ApplicationInstallerWizardController.this).actionPerformed(var(ActionEvent.class));
        }};
        closeDialog = new Closure0() {{
            of(ApplicationInstallerWizardController.this).closeDialog();
        }};
        confirm = new Closure0() {{
            of(ApplicationInstallerWizardController.this).confirm();
        }};
    }

    void closeDialog() {
        if (showExitWarning()) {
            success = false;
            dialog.close();
        }
    }

    synchronized void actionPerformed(final ActionEvent e) {
        TaskManager.getInstance().addNewTask(createNewGuiTask(new Callable<Void>() {
            @Override
            public Void call() {
                navigate(e.getActionCommand().equals(NEXT_STEP_COMMAND.getValue()));
                return null;
            }
        }, "navigate", this));
    }

    private void navigate(final boolean fwd) {
        if ((fwd && !isNextAvailable()) || (!fwd && !isPreviousAvailable())) {
            if (fwd) {
                confirm();
            }
            return;
        }
        StepController<WebApplicationDescriptor> controller = getNextController(fwd);
        if (currentController != null && currentController.equals(controller)) {
            return;
        }
        if (fwd && !controller.isForwardEnabled()) {
            currentIndex.incrementAndGet();
            navigate(true);
        }
        if (currentController != null) {
            if (!currentController.validateInput()) {
                return;
            }
            currentController.beforeNext();
        }
        if (fwd && !controller.canProceed()) {
            return;
        }
        dialog.getActionFor(PREVIOUS_STEP_COMMAND.getValue()).setEnabled(false);
        dialog.getActionFor(NEXT_STEP_COMMAND.getValue()).setEnabled(false);
        dialog.getActionFor(CONFIRM.getValue()).setEnabled(false);
        currentController = controller;
        if (fwd) {
            currentIndex.incrementAndGet();
        }
        currentController.activate();
        wizard.goToStep(currentController.getStep());
        dialog.setHeaderTitle(getMessage(currentController.getTitleKey()));
        dialog.setHeaderDescription(getMessage(currentController.getDescriptionKey()));
        executeStep(fwd);
        dialog.getActionFor(PREVIOUS_STEP_COMMAND.getValue()).setEnabled(isPreviousAvailable());
        dialog.getActionFor(NEXT_STEP_COMMAND.getValue()).setEnabled(!isSummaryStep());
        dialog.getActionFor(CONFIRM.getValue()).setEnabled(isSummaryStep());
    }

    private StepController<WebApplicationDescriptor> getNextController(boolean fwd) {
        return fwd ? nextController() : previousController();
    }

    private void executeStep(boolean fwd) {
        try {
            if (currentController.executionCompleted() && currentController.isExecutionSucceeded() && fwd && currentController.automaticallyProceedToNextStep()) {
                navigate(true);
            }
        } catch (WizardException e) {
            log.error(e.getMessage(), e);
        }
    }

    private boolean showExitWarning() {
        return showWarning(wizard, "wizard.quit.message");
    }

    boolean isSummaryStep() {
        return !isNextAvailable();
    }

    void confirm() {
        success = true;
        dialog.setVisible(false);
    }

    private ApplicationInstallerWizard createWizard() {
        wizard = new ApplicationInstallerWizard();
        return wizard;
    }

    private StepController<WebApplicationDescriptor> nextController() {
        if (!isNextAvailable()) {
            return currentController;
        }
        return controllers.get(currentIndex.get() + 1);
    }

    private StepController<WebApplicationDescriptor> previousController() {
        if (!isPreviousAvailable()) {
            return currentController;
        }
        StepController<WebApplicationDescriptor> controller = controllers.get(currentIndex.decrementAndGet());
        if (!controller.isBackEnabled()) {
            return previousController();
        }
        return controller;
    }

    private boolean isNextAvailable() {
        return currentIndex.get() + 1 < controllers.size();
    }

    private boolean isPreviousAvailable() {
        return currentIndex.get() - 1 >= 0;
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
    }
}
