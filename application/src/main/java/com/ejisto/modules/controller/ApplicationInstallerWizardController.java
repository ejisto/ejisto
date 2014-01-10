/*
 * Ejisto, a powerful developer assistant
 *
 * Copyright (C) 2010-2013 Celestino Bellone
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

import com.ejisto.event.ApplicationEventDispatcher;
import com.ejisto.modules.controller.wizard.StepController;
import com.ejisto.modules.controller.wizard.StepControllerComparator;
import com.ejisto.modules.controller.wizard.installer.*;
import com.ejisto.modules.dao.entities.WebApplicationDescriptor;
import com.ejisto.modules.executor.TaskManager;
import com.ejisto.modules.gui.Application;
import com.ejisto.modules.gui.components.ApplicationInstallerWizard;
import com.ejisto.modules.gui.components.EjistoDialog;
import com.ejisto.modules.gui.components.helper.CallbackAction;
import com.ejisto.modules.repository.CustomObjectFactoryRepository;
import com.ejisto.modules.repository.MockedFieldsRepository;
import com.ejisto.modules.repository.SettingsRepository;
import lombok.extern.log4j.Log4j;

import javax.swing.*;
import java.awt.Dialog.ModalityType;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static com.ejisto.constants.StringConstants.*;
import static com.ejisto.modules.executor.TaskManager.createNewGuiTask;
import static com.ejisto.util.GuiUtils.*;
import static com.ejisto.util.LambdaUtil.callActionPerformed;
import static java.util.Collections.sort;

@Log4j
public class ApplicationInstallerWizardController implements PropertyChangeListener, ActionListener {
    private List<StepController<WebApplicationDescriptor>> controllers;
    private ApplicationInstallerWizard wizard;
    private final Application application;
    private EjistoDialog dialog;
    private AtomicInteger currentIndex = new AtomicInteger(-1);
    private StepController<WebApplicationDescriptor> currentController;
    private boolean success;
    private final String containerHome;
    private final MockedFieldsRepository mockedFieldsRepository;
    private final CustomObjectFactoryRepository customObjectFactoryRepository;
    private final SettingsRepository settingsRepository;
    private final TaskManager taskManager;

    public ApplicationInstallerWizardController(Application application,
                                                String containerHome,
                                                MockedFieldsRepository mockedFieldsRepository,
                                                CustomObjectFactoryRepository customObjectFactoryRepository,
                                                SettingsRepository settingsRepository,
                                                TaskManager taskManager,
                                                ApplicationEventDispatcher eventDispatcher) {
        this.application = application;
        this.containerHome = containerHome;
        this.mockedFieldsRepository = mockedFieldsRepository;
        this.customObjectFactoryRepository = customObjectFactoryRepository;
        this.settingsRepository = settingsRepository;
        this.taskManager = taskManager;
    }

    private void initAndSortControllers(EjistoDialog dialog) {
        controllers = new ArrayList<>();
        controllers.add(new FileSelectionController(dialog, settingsRepository));
        controllers.add(new FileExtractionController(dialog, taskManager));
        controllers.add(new ClassesFilteringController(dialog, taskManager));
        controllers.add(new ApplicationScanningController(dialog, containerHome, mockedFieldsRepository,
                                                          customObjectFactoryRepository, taskManager));
        controllers.add(new PropertiesEditingController(dialog, mockedFieldsRepository));
        controllers.add(new SummaryController(dialog));
        sort(controllers, new StepControllerComparator());
        //setTypes session object
        WebApplicationDescriptor session = new WebApplicationDescriptor();
        session.setContainerId(DEFAULT_CONTAINER_ID.getValue());
        controllers.forEach(c -> c.setSession(session));
    }

    private void initContainer() {
        for (StepController<WebApplicationDescriptor> controller : controllers) {
            wizard.addTab(controller.getView(), controller.getStep());
        }
    }

    public boolean showWizard() {
        dialog = new EjistoDialog(application, getMessage("wizard.title"), createWizard(), false,
                                  "application.installer.icon");
        initAndSortControllers(dialog);
        initContainer();
        dialog.registerAction(new CallbackAction(getMessage("buttons.previous.text"), PREVIOUS_STEP_COMMAND.getValue(),
                                                 callActionPerformed(this), null));
        dialog.registerAction(
                new CallbackAction(getMessage("buttons.next.text"), NEXT_STEP_COMMAND.getValue(), callActionPerformed(this), null));
        Action act = new CallbackAction(getMessage("wizard.ok.text"), CONFIRM.getValue(), e -> confirm(), null);
        act.setEnabled(isSummaryStep());
        dialog.registerAction(act);
        dialog.registerAction(
                new CallbackAction(getMessage("wizard.close.text"), EjistoDialog.CLOSE_ACTION_COMMAND, e -> closeDialog(), null));
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

    void closeDialog() {
        if (showExitWarning()) {
            success = false;
            dialog.close();
        }
    }

    @Override
    public synchronized void actionPerformed(final ActionEvent e) {
        taskManager.addNewTask(createNewGuiTask(() -> {
            navigate(e.getActionCommand().equals(NEXT_STEP_COMMAND.getValue()));
            return null;
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
        if (currentController.executionCompleted() && currentController.isExecutionSucceeded() && fwd && currentController.automaticallyProceedToNextStep()) {
            navigate(true);
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
