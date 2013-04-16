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
import com.ejisto.modules.dao.entities.MockedField;
import com.ejisto.modules.repository.MockedFieldsRepository;
import org.jdesktop.swingx.JXPanel;

import javax.swing.*;
import java.awt.*;
import java.util.List;

import static com.ejisto.modules.gui.components.EjistoDialog.DEFAULT_WIDTH;
import static com.ejisto.modules.gui.components.helper.FieldsEditorContext.ADD_FIELD;
import static com.ejisto.util.GuiUtils.getMessage;
import static java.util.Collections.emptyList;

/**
 * Created by IntelliJ IDEA.
 * User: celestino
 * Date: 11/8/11
 * Time: 5:48 PM
 */
public class MockedFieldSelectionController extends AbstractDialogManager {

    private final MockedFieldsEditorController controller;
    private List<MockedField> selectedFields;

    public MockedFieldSelectionController(MockedFieldsRepository mockedFieldsRepository) {
        super();
        this.controller = new MockedFieldsEditorController(mockedFieldsRepository, ADD_FIELD);
    }

    @Override
    void onAbort() {
        this.selectedFields = emptyList();
    }

    @Override
    void onConfirm() {
        this.selectedFields = controller.getSelection();
    }

    public void showSelectionDialog() {
        openDialog(createPanel(), getMessage("field.add.dialog.title"),
                   getMessage("field.add.dialog.description"), "field.add.icon", new Dimension(DEFAULT_WIDTH, 400));
    }

    private JPanel createPanel() {
        JXPanel content = new JXPanel(new BorderLayout(0, 0));
        content.add(controller.getView(), BorderLayout.CENTER);
        return content;
    }

    public List<MockedField> getSelectedFields() {
        return selectedFields;
    }

}
