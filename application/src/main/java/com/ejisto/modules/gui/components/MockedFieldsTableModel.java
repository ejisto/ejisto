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

import com.ejisto.event.def.BaseApplicationEvent;
import com.ejisto.event.def.MockedFieldCreated;
import com.ejisto.event.def.MockedFieldDeleted;
import com.ejisto.event.def.MockedFieldUpdated;
import com.ejisto.modules.dao.entities.MockedField;
import com.ejisto.modules.gui.components.helper.FieldsEditorContext;
import com.ejisto.util.GuiUtils;

import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;

import static com.ejisto.modules.gui.components.helper.FieldsEditorContext.APPLICATION_INSTALLER_WIZARD;

public class MockedFieldsTableModel extends AbstractTableModel implements TableModelListener {
    private static final long serialVersionUID = 7654333693058889267L;
    public static final int EDITABLE_COLUMN_INDEX = 5;
    private String[] columnHeaders;
    private final List<MockedField> fields;
    private List<List<String>> fieldsAsString;
    private boolean notifyChanges;
    private FieldsEditorContext ctx;

    public MockedFieldsTableModel(List<MockedField> fields, FieldsEditorContext ctx) {
        this.ctx = ctx;
        columnHeaders = ctx.getTableColumns();
        this.fields = new ArrayList<>(fields);
        Collections.sort(this.fields);
        this.fieldsAsString = GuiUtils.asStringList(this.fields, ctx.getColumnFillStrategy());
        addTableModelListener(this);
    }

    @Override
    public int getRowCount() {
        return fields.size();
    }

    @Override
    public int getColumnCount() {
        return columnHeaders.length;
    }

    @Override
    public String getValueAt(int rowIndex, int columnIndex) {
        return fieldsAsString.get(rowIndex).get(columnIndex);
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        String value = String.valueOf(aValue);
        MockedField field = fields.get(rowIndex);
        if (ctx == APPLICATION_INSTALLER_WIZARD) {
            columnIndex += 2;
        }
        switch (columnIndex) {
            case 1:
                field.setContextPath(value);
                break;
            case 2:
                field.setClassName(value);
                break;
            case 3:
                field.setFieldName(value);
                break;
            case 4:
                field.setFieldType(value);
                break;
            case EDITABLE_COLUMN_INDEX:
                field.setFieldValue(value);
                break;
        }
        fireTableCellUpdated(rowIndex, columnIndex);
    }

    @Override
    public void tableChanged(TableModelEvent e) {
        if (notifyChanges) {
            notifyTableChanged(e);
        }
        fieldsAsString = GuiUtils.asStringList(fields, ctx.getColumnFillStrategy());
    }

    private void notifyTableChanged(TableModelEvent e) {
        BaseApplicationEvent event = null;
        final MockedField field = fields.get(e.getFirstRow());
        switch(e.getType()) {
            case TableModelEvent.INSERT:
                event = new MockedFieldCreated(this, field);
                break;
            case TableModelEvent.UPDATE:
                event = new MockedFieldUpdated(this, field);
                break;
            case TableModelEvent.DELETE:
                event = new MockedFieldDeleted(this, field);
                break;
            default:
                break;
        }
        Optional.ofNullable(event).ifPresent(GuiUtils::publishEvent);
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return columnIndex == EDITABLE_COLUMN_INDEX && getMockedFieldAt(rowIndex).isSimpleValue();
    }

    @Override
    public String getColumnName(int column) {
        return columnHeaders[column];
    }

    public MockedField getMockedFieldAt(int row) {
        return fields.get(row);
    }

    public void addFields(List<MockedField> fields) {
        this.fields.addAll(fields);
        Collections.sort(this.fields);
        fireTableDataChanged();
    }

    public void removeFields(List<MockedField> fields) {
        this.fields.removeAll(fields);
    }

}
