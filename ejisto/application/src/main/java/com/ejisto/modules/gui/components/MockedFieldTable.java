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

package com.ejisto.modules.gui.components;

import com.ejisto.modules.dao.entities.MockedField;
import com.ejisto.modules.gui.components.helper.FieldsEditorContext;
import com.ejisto.modules.gui.components.helper.PopupMenuManager;
import org.jdesktop.swingx.JXTable;

import java.awt.*;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: celestino
 * Date: 10/30/11
 * Time: 4:04 PM
 */
public class MockedFieldTable extends JXTable implements MockedFieldsEditorComponent {

    private FieldsEditorContext fieldsEditorContext;

    public MockedFieldTable(FieldsEditorContext fieldsEditorContext) {
        this.fieldsEditorContext = fieldsEditorContext;
        addMouseListener(new PopupMenuManager());
    }

    @Override
    public MockedField getFieldAt(Point point) {
        int rowIndex = rowAtPoint(point);
        if (rowIndex == -1) return null;
        return ((MockedFieldsTableModel) getModel()).getMockedFieldAt(rowIndex);
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
    public void editFieldAt(Point point) {
        editCellAt(rowAtPoint(point), MockedFieldsTableModel.EDITABLE_COLUMN_INDEX);
    }

    @Override
    public void selectFieldAt(Point point) {
        int rowIndex = rowAtPoint(point);
        setRowSelectionInterval(rowIndex, rowIndex);
    }

    @Override
    public List<MockedField> getSelectedFields() {
        return null;
    }

}
