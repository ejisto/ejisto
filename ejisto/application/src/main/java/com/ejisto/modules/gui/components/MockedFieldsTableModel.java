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
package com.ejisto.modules.gui.components;

import static com.ejisto.util.GuiUtils.getMessage;
import static com.ejisto.util.GuiUtils.stringify;
import static com.ejisto.util.SpringBridge.publishApplicationEvent;

import java.util.ArrayList;
import java.util.List;

import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;

import org.springframework.util.Assert;

import com.ejisto.event.def.MockedFieldChanged;
import com.ejisto.modules.dao.entities.MockedField;

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
        this.notifyChanges=notifyChanges;
        this.partial=partial;
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
        if(partial) columnIndex += 2;
        switch(columnIndex) {
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
        if(notifyChanges) notifyTableChanged(e);
    	fieldsAsString=stringify(fields, partial);        
    }
    
    private void notifyTableChanged(TableModelEvent e) {
    	MockedFieldChanged event = new MockedFieldChanged(this, fields.get(e.getFirstRow()));
        publishApplicationEvent(event);
    }
    
    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return columnIndex != 0;
    }
    
    @Override
    public String getColumnName(int column) {
        Assert.isTrue(column < columnHeaders.length);
        return columnHeaders[column];
    }

    
}
