/*
 * Ejisto, a powerful developer assistant
 *
 * Copyright (C) 2010-2012  Celestino Bellone
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

import com.ejisto.event.def.MockedFieldChanged;
import com.ejisto.modules.dao.entities.MockedField;
import com.ejisto.modules.gui.components.helper.FieldsEditorContext;
import com.ejisto.util.GuiUtils;
import org.springframework.util.Assert;

import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.ejisto.modules.gui.components.helper.FieldsEditorContext.APPLICATION_INSTALLER_WIZARD;
import static com.ejisto.util.SpringBridge.publishApplicationEvent;

public class MockedFieldsTableModel extends AbstractTableModel implements TableModelListener {
    private static final long serialVersionUID = 7654333693058889267L;
    public static final int EDITABLE_COLUMN_INDEX = 5;
    private String[] columnHeaders;
    private List<MockedField> fields;
    private List<List<String>> fieldsAsString;
    private boolean notifyChanges;
    private FieldsEditorContext ctx;

    public MockedFieldsTableModel(List<MockedField> fields, FieldsEditorContext ctx) {
        this.ctx = ctx;
        columnHeaders = ctx.getTableColumns();
        this.fields = new ArrayList<MockedField>(fields);
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
        if (ctx == APPLICATION_INSTALLER_WIZARD) columnIndex += 2;
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
        if (notifyChanges) notifyTableChanged(e);
        fieldsAsString = GuiUtils.asStringList(fields, ctx.getColumnFillStrategy());
    }

    private void notifyTableChanged(TableModelEvent e) {
        MockedFieldChanged event = new MockedFieldChanged(this, fields.get(e.getFirstRow()));
        publishApplicationEvent(event);
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return columnIndex == EDITABLE_COLUMN_INDEX && getMockedFieldAt(rowIndex).isSimpleValue();
    }

    @Override
    public String getColumnName(int column) {
        Assert.isTrue(column < columnHeaders.length);
        return columnHeaders[column];
    }

    public MockedField getMockedFieldAt(int row) {
        Assert.isTrue(row < fields.size());
        return fields.get(row);
    }

    public void replaceFields(List<MockedField> fields) {
        for (MockedField field : fields) {
            int index = this.fields.indexOf(field);
            if (index == -1) this.fields.add(field);
            else this.fields.set(index, field);
        }
        Collections.sort(this.fields);
        fireTableDataChanged();
    }

    public void deleteFields(List<MockedField> fields) {
        this.fields.removeAll(fields);
        Collections.sort(this.fields);
    }

}
