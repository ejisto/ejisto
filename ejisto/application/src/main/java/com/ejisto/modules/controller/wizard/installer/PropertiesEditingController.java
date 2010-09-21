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

package com.ejisto.modules.controller.wizard.installer;

import com.ejisto.modules.controller.WizardException;
import com.ejisto.modules.dao.entities.MockedField;
import com.ejisto.modules.gui.components.EjistoDialog;
import com.ejisto.modules.gui.components.MockedFieldsEditor;
import com.ejisto.modules.gui.components.helper.Step;

import java.util.ArrayList;
import java.util.List;

import static ch.lambdaj.Lambda.*;
import static org.hamcrest.Matchers.notNullValue;

public class PropertiesEditingController extends AbstractApplicationInstallerController {

    private MockedFieldsEditor propertiesEditingTab;

    public PropertiesEditingController(EjistoDialog dialog) {
        super(dialog);
    }

    @Override
    public MockedFieldsEditor getView() {
        if(propertiesEditingTab != null) return propertiesEditingTab;
        propertiesEditingTab = new MockedFieldsEditor();
        return propertiesEditingTab;
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
        getView().setFields(new ArrayList<MockedField>(getSession().getFields()));
    }

    @Override
    public boolean executionCompleted() {
        return true;
    }

    @Override
    public void beforeNext() {
        getSession().setModifiedFields(getModifiedFields());
    }
    
    private List<MockedField> getModifiedFields() {
        return select(getSession().getFields(), having(on(MockedField.class).getFieldValue(), notNullValue()));
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
