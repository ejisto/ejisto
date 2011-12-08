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

import com.ejisto.modules.controller.MockedFieldsEditorController;
import com.ejisto.modules.controller.WizardException;
import com.ejisto.modules.dao.entities.JndiDataSource;
import com.ejisto.modules.dao.entities.MockedField;
import com.ejisto.modules.gui.components.EjistoDialog;
import com.ejisto.modules.gui.components.MockedFieldsEditor;
import com.ejisto.modules.gui.components.helper.Step;
import com.ejisto.modules.validation.DataSourceEnvEntryValidator;
import com.ejisto.modules.validation.ValidationErrors;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class PropertiesEditingController extends AbstractApplicationInstallerController {

    private MockedFieldsEditorController editorController;

    public PropertiesEditingController(EjistoDialog dialog) {
        super(dialog);
    }

    @Override
    public MockedFieldsEditor getView() {
        if (editorController != null) return editorController.getView();
        editorController = new MockedFieldsEditorController();
        return editorController.getView();
    }

    @Override
    public boolean canProceed() {
        return validateEnvEntries();
    }

    private boolean validateEnvEntries() {
        List<JndiDataSource> entries = getSession().getDataSources();
        if (CollectionUtils.isEmpty(entries)) return true;
        DataSourceEnvEntryValidator validator = new DataSourceEnvEntryValidator();
        ValidationErrors errors = new ValidationErrors("JndiDataSource");
        validator.validateAll(entries, errors);
        return !errors.hasErrors();
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
        getView().setFields(new ArrayList<MockedField>(getSession().getFields()));
    }

    @Override
    public void beforeNext() {

    }

    private Collection<MockedField> getModifiedFields() {
        return getSession().getModifiedFields();
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
