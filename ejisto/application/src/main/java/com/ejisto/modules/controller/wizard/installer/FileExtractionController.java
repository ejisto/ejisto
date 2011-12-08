/*
 * Ejisto, a powerful developer assistant
 *
 * Copyright (C) 2010-2011  Celestino Bellone
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
import com.ejisto.modules.controller.wizard.installer.workers.FileExtractionWorker;
import com.ejisto.modules.executor.ProgressDescriptor;
import com.ejisto.modules.executor.Task;
import com.ejisto.modules.gui.components.EjistoDialog;
import com.ejisto.modules.gui.components.ProgressPanel;
import com.ejisto.modules.gui.components.helper.Step;

import java.beans.PropertyChangeEvent;

import static com.ejisto.util.GuiUtils.getMessage;

public class FileExtractionController extends AbstractApplicationInstallerController {

    private ProgressPanel fileExtractionTab;

    public FileExtractionController(EjistoDialog dialog) {
        super(dialog);
    }

    @Override
    public ProgressPanel getView() {
        if (fileExtractionTab != null) return fileExtractionTab;
        fileExtractionTab = new ProgressPanel();
        return fileExtractionTab;
    }

    @Override
    public boolean canProceed() {
        return getSession().getWarFile() != null;
    }

    @Override
    public boolean automaticallyProceedToNextStep() {
        return false;
    }

    @Override
    protected Task<?> createNewTask() {
        return new FileExtractionWorker(getSession(), this);
    }

    @Override
    public boolean isExecutionSucceeded() throws WizardException {
        return isDone();
    }

    @Override
    public Step getStep() {
        return Step.FILE_EXTRACTION;
    }

    @Override
    public void activate() {

    }

    @Override
    public String getTitleKey() {
        return "wizard.fileextraction.title";
    }

    @Override
    public String getDescriptionKey() {
        return "wizard.fileextraction.description";
    }

    @Override
    public boolean isBackEnabled() {
        return false;
    }

    @Override
    protected void handlePropertyChange(PropertyChangeEvent event) {
        String propertyName = event.getPropertyName();
        if (propertyName.equals("startProgress")) {
            notifyStart((Integer) event.getNewValue());
        } else if (propertyName.equals("progressDescriptor")) {
            ProgressDescriptor descriptor = (ProgressDescriptor) event.getNewValue();
            if (descriptor.isTaskCompleted()) {
                getView().processCompleted(
                        getMessage("progress.file.extraction.end", getSession().getWarFile().getName()));
            } else {
                writeProgress(descriptor.getMessage());
            }
        }
    }

    private void notifyStart(final int numJobs) {
        getView().initProgress(numJobs, getMessage("progress.start"));
    }

    private void writeProgress(String message) {
        getView().jobCompleted(message);
    }
}
