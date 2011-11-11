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

import ch.lambdaj.group.Group;
import com.ejisto.event.def.MockedFieldChanged;
import com.ejisto.event.def.StatusBarMessage;
import com.ejisto.modules.dao.entities.MockedField;
import com.ejisto.modules.gui.components.helper.FieldsEditorContext;
import com.ejisto.modules.gui.components.helper.PopupMenuManager;
import com.ejisto.modules.validation.MockedFieldValidator;
import com.ejisto.modules.validation.ValidationErrors;
import org.apache.log4j.Logger;
import org.springframework.validation.Errors;

import javax.swing.*;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static ch.lambdaj.Lambda.*;
import static com.ejisto.modules.gui.components.helper.FieldsEditorContext.MAIN_WINDOW;
import static com.ejisto.util.GuiUtils.getMessage;
import static com.ejisto.util.SpringBridge.publishApplicationEvent;

public class MockedFieldTree extends JTree implements CellEditorListener, MockedFieldsEditorComponent {
    private static final long serialVersionUID = 3542351125591491996L;
    private static final Logger logger = Logger.getLogger(MockedFieldTree.class);
    private String rootText = getMessage("wizard.properties.editor.tab.hierarchical.rootnode");
    private MockedFieldValidator validator;
    private JTextField textField;
    private FieldsEditorContext fieldsEditorContext;

    public MockedFieldTree(FieldsEditorContext fieldsEditorContext) {
        super(new Object[0]);
        setCellEditor(new MockedFieldCellEditor(getTextField()));
        this.validator = new MockedFieldValidator();
        this.fieldsEditorContext = fieldsEditorContext;
        addMouseListener(new PopupMenuManager());
    }

    @Override
    public String convertValueToText(Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
        if (EmptyTreeNode.class.isAssignableFrom(node.getClass()))
            return getMessage("main.propertieseditor.tree.novalues.text");
        node = (DefaultMutableTreeNode) scanNode(node);
        if (node == null) return super.convertValueToText(value, selected, expanded, leaf, row, hasFocus);
        if (!leaf) return row == 0 && fieldsEditorContext == MAIN_WINDOW ? rootText : node.toString();
        return ((MockedField) node.getUserObject()).getCompleteDescription();
    }

    @Override
    public void setFields(List<MockedField> fields) {
        setModel(new DefaultTreeModel(mockedFields2Nodes(fields)));
        getCellEditor().addCellEditorListener(this);
        setExpandedState(getUI().getPathForRow(this, 0), true);
    }

    @Override
    public void editFieldAt(Point point) {
        startEditingAtPath(getPathForLocation(point.x, point.y));
    }

    @Override
    public void selectFieldAt(Point point) {
        setSelectionPath(getPathForLocation(point.x, point.y));
    }

    @Override
    public List<MockedField> getSelectedFields() {
        return Arrays.asList(getSelectedField());
    }

    /**
     * Utility method that converts a <b>sorted</b> Collection of MockedField in
     * a List of TreeNode
     *
     * @param in Collection of MockedFields
     * @return Tree structure
     */
    private TreeNode mockedFields2Nodes(Collection<MockedField> in) {
        if (in.isEmpty()) return new EmptyTreeNode();
        Group<MockedField> groupedFields = group(in, by(on(MockedField.class).getPackageName()),
                                                 by(on(MockedField.class).getClassSimpleName()),
                                                 by(on(MockedField.class).getFieldName()));
        return group2Node(groupedFields, 0);
    }

    private MockedFieldNode group2Node(Group<MockedField> group, int depth) {
        MockedFieldNode node;
        if (group.isLeaf()) {
            node = new MockedFieldNode(group.first());
        } else {
            node = new MockedFieldNode(group.first(), depth < 2);
            for (Group<MockedField> child : group.subgroups())
                node.add(group2Node(child, depth + 1));
        }
        return node;
    }

    @Override
    public boolean isPathEditable(TreePath path) {
        if (!isEditable() || !getModel().isLeaf(path.getLastPathComponent())) return false;
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) scanNode((TreeNode) path.getLastPathComponent());
        return node != null && ((MockedField) node.getUserObject()).isSimpleValue();
    }

    @Override
    public void editingCanceled(ChangeEvent e) {
        if (logger.isDebugEnabled()) logger.debug("editing canceled");
    }

    @Override
    public void editingStopped(ChangeEvent e) {
        TreePath editingPath = getEditingPath();
        if (editingPath == null) return;
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) scanNode((TreeNode) editingPath.getLastPathComponent());
        if (node == null) return;
        MockedField mf = (MockedField) node.getUserObject();
        String previousType = mf.getFieldType();
        String previousValue = mf.getFieldValue();
        mf.setFieldValue(String.valueOf(getCellEditor().getCellEditorValue()));
        Errors errors = new ValidationErrors("MockedField");
        validator.validate(mf, errors);
        if (errors.hasErrors()) {
            mf.setFieldValue(previousValue);
            mf.setFieldType(previousType);
            publishApplicationEvent(new StatusBarMessage(this,
                                                         getMessage("propertieseditor.invalid.input",
                                                                    String.valueOf(cellEditor.getCellEditorValue()),
                                                                    mf.getFieldName()), true));
        }
        ((DefaultTreeModel) getModel()).reload();
        setSelectionPath(editingPath);
        if (fieldsEditorContext.isNotifyChangeNeeded()) {
            MockedFieldChanged event = new MockedFieldChanged(this, mf);
            publishApplicationEvent(event);
        }
    }

    public void redraw(int targetX, int targetY) {
        TreePath path = getPathForLocation(targetX, targetY);
        ((DefaultTreeModel) getModel()).reload();
        setSelectionPath(path);
    }

    private TreeNode scanNode(TreeNode node) {
        if (node == null || !isDefaultMutableTreeNode(node)) return null;
        Object userObject = ((DefaultMutableTreeNode) node).getUserObject();
        if (!MockedField.class.isAssignableFrom(userObject.getClass())) return scanNode(node.getParent());
        return node;
    }

    private boolean isDefaultMutableTreeNode(TreeNode node) {
        return DefaultMutableTreeNode.class.isAssignableFrom(node.getClass());
    }

    public MockedField getSelectedField() {
        TreePath path = getSelectionPath();
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
        return (MockedField) node.getUserObject();
    }

    private JTextField getTextField() {
        if (this.textField != null) return this.textField;
        textField = new JTextField();
        textField.setPreferredSize(new Dimension(300, 20));
        return textField;
    }

    @Override
    public MockedField getFieldAt(Point point) {
        return getFieldAt(point.x, point.y);
    }

    @Override
    public MockedField getFieldAt(int x, int y) {
        TreePath path = getPathForLocation(x, y);
        if (path == null) return null;
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) scanNode((TreeNode) path.getLastPathComponent());
        if (node == null) return null;
        return (MockedField) node.getUserObject();
    }

    private static final class MockedFieldCellEditor extends DefaultCellEditor {
        private static final long serialVersionUID = 8403949313719981178L;

        public MockedFieldCellEditor(JTextField textField) {
            super(textField);
        }

        @Override
        public void addCellEditorListener(CellEditorListener l) {
            super.addCellEditorListener(l);
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

    private static final class MockedFieldNode extends DefaultMutableTreeNode {
        private static final long serialVersionUID = 1L;
        private MockedField field;
        private boolean head;

        public MockedFieldNode(MockedField userObject) {
            this(userObject, false);
        }

        public MockedFieldNode(MockedField userObject, boolean head) {
            super(userObject);
            this.field = userObject;
            this.head = head;
        }

        @Override
        public String toString() {
            return head ? field.getPackageName() : field.getClassSimpleName();
        }
    }
}
