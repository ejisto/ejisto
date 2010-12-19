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

package com.ejisto.modules.gui.components;

import com.ejisto.event.def.MockedFieldChanged;
import com.ejisto.modules.dao.entities.MockedField;
import org.springframework.util.Assert;

import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;

import static com.ejisto.util.GuiUtils.getMessage;
import static com.ejisto.util.GuiUtils.stringify;
import static com.ejisto.util.SpringBridge.publishApplicationEvent;

public class MockedFieldsTableModel extends AbstractTableModel implements TableModelListener {
    private static final long serialVersionUID = 7654333693058889267L;
    private String[] columnHeaders;
    private List<MockedField> fields;
    private List<List<String>> fieldsAsString;
    private boolean notifyChanges;
    private boolean partial;

    public MockedFieldsTableModel(List<MockedField> fields) {
        this(fields, true, false);
    }

    public MockedFieldsTableModel(List<MockedField> fields, boolean notifyChanges) {
        this(fields, notifyChanges, false);
    }

    public MockedFieldsTableModel(List<MockedField> fields, boolean notifyChanges, boolean partial) {
        columnHeaders = getMessage(partial ? "wizard.properties.editor.columns" : "main.tab.property.columns").split(",");
        this.fields = new ArrayList<MockedField>(fields);
        this.fieldsAsString = stringify(this.fields, partial);
        addTableModelListener(this);
        this.notifyChanges = notifyChanges;
        this.partial = partial;
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
        if (partial) columnIndex += 2;
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
            case 5:
                field.setFieldValue(value);
                break;
        }
        fireTableCellUpdated(rowIndex, columnIndex);
    }


    @Override
    public void tableChanged(TableModelEvent e) {
        if (notifyChanges) notifyTableChanged(e);
        fieldsAsString = stringify(fields, partial);
    }

    private void notifyTableChanged(TableModelEvent e) {
        MockedFieldChanged event = new MockedFieldChanged(this, fields.get(e.getFirstRow()));
        publishApplicationEvent(event);
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return columnIndex == 5;
    }

    @Override
    public String getColumnName(int column) {
        Assert.isTrue(column < columnHeaders.length);
        return columnHeaders[column];
    }

    public void refresh() {
        fieldsAsString = stringify(fields, partial);
    }

}
