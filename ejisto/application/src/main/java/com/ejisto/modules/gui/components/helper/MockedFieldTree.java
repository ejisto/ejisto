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

import ch.lambdaj.group.Group;
import com.ejisto.event.def.MockedFieldChanged;
import com.ejisto.event.def.StatusBarMessage;
import com.ejisto.modules.dao.entities.MockedField;
import com.ejisto.modules.validation.MockedFieldValidator;
import com.ejisto.modules.validation.ValidationErrors;
import org.springframework.validation.Errors;

import javax.swing.*;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.tree.*;
import java.awt.*;
import java.util.Collection;
import java.util.List;

import static ch.lambdaj.Lambda.*;
import static com.ejisto.util.GuiUtils.getMessage;
import static com.ejisto.util.SpringBridge.publishApplicationEvent;

public class MockedFieldTree extends JTree implements CellEditorListener {
    private static final long serialVersionUID = 3542351125591491996L;
    private String rootText = getMessage("wizard.properties.editor.tab.hierarchical.rootnode");
    private JTextField textField;
    private MockedFieldValidator validator;
    private boolean notifyChanges;

    public MockedFieldTree(boolean notifyChanges) {
        super();
        this.validator = new MockedFieldValidator();
        setCellEditor(new MockedFieldCellEditor(getTextField()));
        this.notifyChanges = notifyChanges;
    }

    private JTextField getTextField() {
        if (this.textField != null) return this.textField;
        textField = new JTextField();
        textField.setPreferredSize(new Dimension(300, 20));
        return textField;
    }

    @Override
    public String convertValueToText(Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
        if (EmptyTreeNode.class.isAssignableFrom(node.getClass()))
            return getMessage("main.propertieseditor.tree.novalues.text");
        if (!MockedField.class.isAssignableFrom(node.getUserObject().getClass()))
            return super.convertValueToText(value, selected, expanded, leaf, row, hasFocus);
        MockedField mockedField = (MockedField) ((DefaultMutableTreeNode) value).getUserObject();
        if (!leaf) return row == 0 ? rootText : mockedField.getClassName();
        return new StringBuilder(mockedField.getFieldName()).append(" [").append(mockedField.getFieldType()).append("]: ").append(mockedField.getFieldValue())
                .toString();
    }

    public void setFields(List<MockedField> fields) {
        setModel(new DefaultTreeModel(mockedFields2Nodes(fields)));
        getCellEditor().addCellEditorListener(this);
    }

    /**
     * Utility method that converts a <b>sorted</b> Collection of MockedField in
     * a List of TreeNode
     *
     * @param in
     * @return
     */
    private TreeNode mockedFields2Nodes(Collection<MockedField> in) {
        if (in.isEmpty()) return new EmptyTreeNode();
        Group<MockedField> groupedFields = group(in, by(on(MockedField.class).getGroupKey()), by(on(MockedField.class).getFieldName()));
        return group2Node(groupedFields);
    }

    private DefaultMutableTreeNode group2Node(Group<MockedField> group) {
        DefaultMutableTreeNode node;
        if (group.isLeaf()) {
            node = new DefaultMutableTreeNode(group.first());
        } else {
            node = new DefaultMutableTreeNode(group.first());
            for (Group<MockedField> child : group.subgroups()) {
                node.add(group2Node(child));
            }
        }
        return node;
    }

    @Override
    public void editingCanceled(ChangeEvent e) {

    }

    @Override
    public boolean isPathEditable(TreePath path) {
        return isEditable() && getModel().isLeaf(path.getLastPathComponent());
    }

    @Override
    public void editingStopped(ChangeEvent e) {
        TreeCellEditor editor = getCellEditor();
        TreePath editingPath = getEditingPath();
        if (editingPath == null) return;
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) editingPath.getLastPathComponent();
        MockedField mf = (MockedField) node.getUserObject();
        String previous = mf.getFieldValue();
        mf.setFieldValue(String.valueOf(editor.getCellEditorValue()));
        Errors errors = new ValidationErrors("MockedField");
        validator.validate(mf, errors);
        if (errors.hasErrors()) {
            mf.setFieldValue(previous);
            publishApplicationEvent(new StatusBarMessage(this, getMessage("propertieseditor.invalid.input", String.valueOf(editor.getCellEditorValue()), mf.getFieldName()), true));
        }

        ((DefaultTreeModel) getModel()).reload(node.getParent());
        setSelectionPath(editingPath);
        if (notifyChanges) {
            MockedFieldChanged event = new MockedFieldChanged(this, mf);
            publishApplicationEvent(event);
        }

    }

    private static final class MockedFieldCellEditor extends DefaultCellEditor {
        private static final long serialVersionUID = 8403949313719981178L;

        public MockedFieldCellEditor(JTextField textField) {
            super(textField);
        }

        @Override
        public Component getTreeCellEditorComponent(JTree tree, Object value, boolean isSelected, boolean expanded, boolean leaf, int row) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
            if (!MockedField.class.isAssignableFrom(node.getUserObject().getClass()))
                return super.getTreeCellEditorComponent(tree, value, isSelected, expanded, leaf, row);
            super.delegate.setValue(((MockedField) node.getUserObject()).getFieldValue());
            return super.editorComponent;
        }
    }

    private static final class EmptyTreeNode extends DefaultMutableTreeNode {
        private static final long serialVersionUID = 1L;

    }
}
