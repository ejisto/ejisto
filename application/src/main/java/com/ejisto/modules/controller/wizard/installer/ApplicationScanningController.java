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

package com.ejisto.modules.controller.wizard.installer;

import com.ejisto.modules.controller.WizardException;
import com.ejisto.modules.controller.wizard.installer.workers.ApplicationScanningWorker;
import com.ejisto.modules.executor.ErrorDescriptor;
import com.ejisto.modules.executor.ProgressDescriptor;
import com.ejisto.modules.executor.Task;
import com.ejisto.modules.gui.components.EjistoDialog;
import com.ejisto.modules.gui.components.ProgressPanel;
import com.ejisto.modules.gui.components.helper.Step;

import java.beans.PropertyChangeEvent;
import java.util.concurrent.atomic.AtomicReference;

import static com.ejisto.modules.executor.ProgressDescriptor.ProgressState.INDETERMINATE;
import static com.ejisto.util.GuiUtils.getMessage;
import static com.ejisto.util.GuiUtils.runOnEDT;

public class ApplicationScanningController extends AbstractApplicationInstallerController {
    private ProgressPanel applicationScanningTab;
    private final String containerHome;
    private AtomicReference<ProgressDescriptor.ProgressState> progressState = new AtomicReference<>(
            INDETERMINATE);

    public ApplicationScanningController(EjistoDialog dialog, String containerHome) {
        super(dialog);
        this.containerHome = containerHome;
    }

    @Override
    public ProgressPanel getView() {
        if (applicationScanningTab != null) {
            return applicationScanningTab;
        }
        applicationScanningTab = new ProgressPanel();
        return applicationScanningTab;
    }

    @Override
    public boolean canProceed() {
        return true;
    }

    @Override
    public boolean isExecutionSucceeded() throws WizardException {
        return super.isDone();
    }

    @Override
    public Step getStep() {
        return Step.APPLICATION_SCANNING;
    }

    @Override
    public void activate() {
        getView().reset();
    }

    @Override
    protected Task<?> createNewTask() {
        return new ApplicationScanningWorker(this, containerHome);
    }

    private void notifyStart(final int numJobs) {
        getView().initProgress(numJobs, getMessage("progress.scan.start"));
    }

    private void notifyJobCompleted(String nextClass) {
        writeProgress(getMessage("progress.scan.class", nextClass));
    }

    private void writeProgress(final String message) {
        getView().jobCompleted(message);
    }

    @Override
    public String getTitleKey() {
        return "wizard.applicationscanning.title";
    }

    @Override
    public String getDescriptionKey() {
        return "wizard.applicationscanning.description";
    }

    @Override
    public boolean isBackEnabled() {
        return false;
    }

    @Override
    protected void handlePropertyChange(final PropertyChangeEvent event) {
        String propertyName = event.getPropertyName();
        switch (propertyName) {
            case "startProgress":
                notifyStart((Integer) event.getNewValue());
                break;
            case "progressDescriptor":
                ProgressDescriptor descriptor = (ProgressDescriptor) event.getNewValue();
                syncProgressState(descriptor.getProgressState());
                if (descriptor.isTaskCompleted()) {
                    getView().processCompleted(getMessage("progress.scan.end"));
                } else if (descriptor.isIndeterminate()) {
                    writeProgress(descriptor.getMessage());
                } else {
                    notifyJobCompleted(descriptor.getMessage());
                }

                break;
            case "error":
                runOnEDT(new Runnable() {
                    @Override
                    public void run() {
                        getView().addError((ErrorDescriptor) event.getNewValue());
                    }
                });
                break;
        }
    }

    private void syncProgressState(ProgressDescriptor.ProgressState current) {
        ProgressDescriptor.ProgressState previous = progressState.get();
        boolean success = previous != current && progressState.compareAndSet(previous, current);
        if (success && current == INDETERMINATE) {
            getView().reset();
        }
    }

}
