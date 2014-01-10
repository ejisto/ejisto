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

package com.ejisto.modules.gui.components;

import com.ejisto.modules.dao.entities.MockedField;
import com.ejisto.modules.gui.components.helper.*;
import com.ejisto.util.GuiUtils;
import org.jdesktop.swingx.JXTable;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.ejisto.modules.gui.components.helper.FieldsEditorContext.ADD_FIELD;
import static java.util.Collections.emptyList;

/**
 * Created by IntelliJ IDEA.
 * User: celestino
 * Date: 10/30/11
 * Time: 4:04 PM
 */
public class MockedFieldTable extends JXTable implements MockedFieldsEditorComponent {

    private final FieldsEditorContext fieldsEditorContext;
    private final transient MockedFieldEditingEventHelper helper;

    public MockedFieldTable(FieldsEditorContext fieldsEditorContext) {
        super(new MockedFieldsTableModel(Collections.<MockedField>emptyList(), fieldsEditorContext));
        this.fieldsEditorContext = fieldsEditorContext;
        addMouseListener(new PopupMenuManager());
        if (fieldsEditorContext == ADD_FIELD) {
            setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        }
        this.helper = new MockedFieldEditingEventHelper();
    }

    @Override
    public boolean hasEditableFieldAtLocation(Point point) {
        return fieldsEditorContext.isEditable() && getFieldAt(point) != null;
    }

    @Override
    public MockedField getFieldAt(Point point) {
        int rowIndex = rowAtPoint(point);
        if (rowIndex == -1) {
            return null;
        }
        return getModel().getMockedFieldAt(rowIndex);
    }

    @Override
    public MockedField getFieldAt(int x, int y) {
        return getFieldAt(new Point(x, y));
    }

    @Override
    public void setFields(List<MockedField> fields) {
        setModel(new MockedFieldsTableModel(fields, fieldsEditorContext));
    }

    @Override
    public void editFieldAt(final Point point) {
        MockedField field = getFieldAt(point);
        if (!field.isSimpleValue()) {
            fireEditingStarted(field, point);
        } else {
            GuiUtils.runOnEDT(() -> {
                editCellAt(rowAtPoint(point), MockedFieldsTableModel.EDITABLE_COLUMN_INDEX);
                ((JTextField) getEditorComponent()).grabFocus();
            });
        }
    }

    @Override
    public void selectFieldAt(Point point) {
        int rowIndex = rowAtPoint(point);
        if (rowIndex > -1) {
            setRowSelectionInterval(rowIndex, rowIndex);
        }
    }

    @Override
    public List<MockedField> getSelectedFields() {
        int[] selectedRows = getSelectedRows();
        if (selectedRows.length == 0) {
            return emptyList();
        }
        List<MockedField> selectedFields = new ArrayList<>(selectedRows.length);
        for (int selectedRow : selectedRows) {
            selectedFields.add(getModel().getMockedFieldAt(selectedRow));
        }
        return selectedFields;
    }

    @Override
    public void addFieldEditingListener(FieldEditingListener fieldEditingListener) {
        helper.addFieldEditingListener(fieldEditingListener);
    }

    @Override
    public void removeFieldEditingListener(FieldEditingListener fieldEditingListener) {
        helper.removeFieldEditingListener(fieldEditingListener);
    }

    @Override
    public MockedFieldsTableModel getModel() {
        return (MockedFieldsTableModel) super.getModel();
    }

    @Override
    public void fieldsChanged(List<MockedField> fields) {
        getModel().replaceFields(fields);
    }

    @Override
    public void contextInstalled(String contextPath, List<MockedField> fields) {
        fieldsChanged(fields);
    }

    @Override
    public void contextRemoved(String contextPath, List<MockedField> fields) {
        getModel().deleteFields(fields);
    }

    @Override
    public boolean fillWithCustomMenuItems(JPopupMenu menu, Point sourcePosition) {
        return false;
    }

    @Override
    public Component toComponent() {
        return this;
    }

    @Override
    public FieldsEditorContext getCurrentEditorContext() {
        return fieldsEditorContext;
    }

    private void fireEditingStarted(MockedField field, Point point) {
        MockedFieldEditingEvent event = new MockedFieldEditingEvent(this, field, fieldsEditorContext, point);
        helper.fireEditingStarted(event);
    }

}
