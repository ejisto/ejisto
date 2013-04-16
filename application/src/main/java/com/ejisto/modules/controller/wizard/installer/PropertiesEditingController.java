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

import com.ejisto.modules.controller.MockedFieldsEditorController;
import com.ejisto.modules.controller.WizardException;
import com.ejisto.modules.gui.components.EjistoDialog;
import com.ejisto.modules.gui.components.MockedFieldsEditor;
import com.ejisto.modules.gui.components.helper.Step;
import com.ejisto.modules.repository.MockedFieldsRepository;

import java.util.ArrayList;

public class PropertiesEditingController extends AbstractApplicationInstallerController {

    private final MockedFieldsEditorController editorController;


    public PropertiesEditingController(EjistoDialog dialog,
                                       MockedFieldsRepository mockedFieldsRepository) {
        super(dialog, null);
        this.editorController = new MockedFieldsEditorController(mockedFieldsRepository);
    }

    @Override
    public MockedFieldsEditor getView() {
        return editorController.getView();
    }

    @Override
    public boolean canProceed() {
        return true;
    }

    @Override
    public boolean isExecutionSucceeded() throws WizardException {
        return true;
    }

    @Override
    public Step getStep() {
        return Step.PROPERTIES_EDITING;
    }

    @Override
    public void activate() {
        editorController.setWizardFields(getSession().getFields());
        getView().setFields(new ArrayList<>(getSession().getFields()));
    }

    @Override
    public void beforeNext() {

    }

    @Override
    public String getTitleKey() {
        return "wizard.propertiesedit.title";
    }

    @Override
    public String getDescriptionKey() {
        return "wizard.propertiesedit.description";
    }
}
