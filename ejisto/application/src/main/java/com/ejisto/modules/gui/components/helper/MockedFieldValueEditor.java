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

package com.ejisto.modules.gui.components.helper;

import com.ejisto.modules.dao.entities.MockedField;
import com.ejisto.modules.repository.MockedFieldsRepository;

import javax.swing.*;
import javax.swing.table.TableCellEditor;
import javax.swing.tree.TreeCellEditor;
import java.awt.*;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: celestino
 * Date: 12/19/10
 * Time: 11:52 AM
 */
public class MockedFieldValueEditor extends AbstractCellEditor implements TableCellEditor, TreeCellEditor {

    private List<MockedField> fields;
    private EditorManager editorManager;

    public MockedFieldValueEditor(List<MockedField> fields) {
        this.fields = fields;
    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        if (!isSelected || !table.isCellEditable(row, column)) return null;
        return getEditorFor(row);
    }

    @Override
    public Component getTreeCellEditorComponent(JTree tree, Object value, boolean isSelected, boolean expanded, boolean leaf, int row) {
        if (!tree.isEditable() || !isSelected || !leaf) return null;
        return getEditorFor(row);
    }

    @Override
    public Object getCellEditorValue() {
        return editorManager.getEditorValue();
    }

    private Component getEditorFor(int row) {
        MockedField m = fields.get(row);
        editorManager = new EditorManager(m);
        return editorManager.getEditor();
    }

    private static final class EditorManager {
        private boolean simpleValue;
        private MockedField mockedField;
        private JTextField simpleEditor;
        private MockedFieldValueEditorPanel complexEditor;

        public EditorManager(MockedField mockedField) {
            this.simpleValue = mockedField.isSimpleValue();
            this.mockedField = mockedField;
        }

        public JComponent getEditor() {
            return simpleValue ? getSimpleEditor() : getComplexEditor().getEditor();
        }

        public Object getEditorValue() {
            if (simpleValue) return getSimpleEditor().getText();
            return getComplexEditor().getValueAsString();
        }

        private JTextField getSimpleEditor() {
            if (this.simpleEditor == null) {
                simpleEditor = new JTextField();
                simpleEditor.setPreferredSize(new Dimension(300, 20));
            }
            simpleEditor.setText(mockedField.getFieldValue());
            return simpleEditor;
        }

        private MockedFieldValueEditorPanel getComplexEditor() {
            if (this.complexEditor == null) {
                complexEditor = new MockedFieldValueEditorPanel(mockedField, MockedFieldsRepository.getInstance().findAllMockedTypes(mockedField.getContextPath()));
                complexEditor.getEditor().setPreferredSize(new Dimension(300, 100));
            }
            return complexEditor;
        }
    }
}
