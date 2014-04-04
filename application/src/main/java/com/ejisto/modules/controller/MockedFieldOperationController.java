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

import com.ejisto.event.def.MockedFieldDeleted;
import com.ejisto.event.def.MockedFieldOperation;
import com.ejisto.event.def.MockedFieldUpdated;
import com.ejisto.modules.dao.entities.MockedField;
import com.ejisto.modules.repository.MockedFieldsRepository;
import com.ejisto.util.GuiUtils;
import org.apache.commons.collections4.CollectionUtils;

import java.awt.*;
import java.util.List;
import java.util.Optional;

import static com.ejisto.util.GuiUtils.getMessage;

/**
 * Created by IntelliJ IDEA.
 * User: celestino
 * Date: 11/3/11
 * Time: 8:33 AM
 */
public class MockedFieldOperationController {

    private final MockedField field;
    private final MockedFieldOperation.OperationType operationType;
    private final Window container;
    private final MockedFieldsRepository mockedFieldsRepository;
    private final Optional<MockedField> selectedFieldOnContainer;

    public MockedFieldOperationController(Window container,
                                          MockedFieldsRepository mockedFieldsRepository,
                                          MockedFieldOperation event) {
        this.field = event.getMockedField();
        this.operationType = event.getOperationType();
        this.container = container;
        this.mockedFieldsRepository = mockedFieldsRepository;
        this.selectedFieldOnContainer = event.getSource().getSelectedFields().stream().findFirst();
    }

    public void showDialog() {
        switch (operationType) {
            case DELETE:
                deleteField();
                break;
            case ADD:
                activateFields();
                break;
            case CREATE:
                createField(selectedFieldOnContainer);
                break;
            default:
                break;
        }
    }

    private void deleteField() {
        if (GuiUtils.showWarning(container, getMessage("warning.message"))) {
            field.setActive(false);
            GuiUtils.publishEvent(new MockedFieldDeleted(container, field));
        }
    }

    private void activateFields() {
        MockedFieldSelectionController selectionController = new MockedFieldSelectionController(mockedFieldsRepository);
        selectionController.showSelectionDialog();
        List<MockedField> selectedFields = selectionController.getSelectedFields();
        if (!CollectionUtils.isEmpty(selectedFields)) {
            selectedFields.forEach(f -> f.setActive(true));
            GuiUtils.publishEvent(new MockedFieldUpdated(container, selectedFields));
        }
    }

    private void createField(Optional<MockedField> selectedFieldOnContainer) {
        MockedFieldCreationController controller = new MockedFieldCreationController(mockedFieldsRepository, selectedFieldOnContainer);
        controller.showCreateDialog();
    }

}
