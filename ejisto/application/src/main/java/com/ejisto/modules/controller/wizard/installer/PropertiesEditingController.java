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
package com.ejisto.modules.controller.wizard.installer;

import static ch.lambdaj.Lambda.having;
import static ch.lambdaj.Lambda.on;
import static ch.lambdaj.Lambda.select;
import static org.hamcrest.Matchers.notNullValue;

import java.util.ArrayList;
import java.util.List;

import com.ejisto.modules.controller.WizardException;
import com.ejisto.modules.dao.entities.MockedField;
import com.ejisto.modules.gui.components.EjistoDialog;
import com.ejisto.modules.gui.components.MockedFieldsEditor;
import com.ejisto.modules.gui.components.helper.Step;

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
