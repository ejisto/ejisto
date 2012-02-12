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
import com.ejisto.event.def.StatusBarMessage;
import com.ejisto.modules.dao.entities.MockedField;
import com.ejisto.modules.gui.components.helper.*;
import com.ejisto.modules.validation.MockedFieldValidator;
import com.ejisto.modules.validation.ValidationErrors;
import lombok.extern.log4j.Log4j;
import org.springframework.validation.Errors;

import javax.swing.*;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;

import static com.ejisto.modules.gui.components.helper.FillStrategies.applyStrategy;
import static com.ejisto.modules.gui.components.helper.FillStrategies.bestStrategyFor;
import static com.ejisto.modules.gui.components.helper.MockedFieldTreeElementsBuilder.EmptyTreeNode;
import static com.ejisto.util.GuiUtils.getIcon;
import static com.ejisto.util.GuiUtils.getMessage;
import static com.ejisto.util.SpringBridge.publishApplicationEvent;

@Log4j
public class MockedFieldTree extends JTree implements CellEditorListener, MockedFieldsEditorComponent {
    private static final long serialVersionUID = 3542351125591491996L;
    private MockedFieldValidator validator;
    private JTextField textField;
    private FieldsEditorContext fieldsEditorContext;
    private final List<FieldEditingListener> editingListeners = new ArrayList<FieldEditingListener>();

    public MockedFieldTree(FieldsEditorContext fieldsEditorContext) {
        super(new MockedFieldNode(true));
        setCellEditor(new MockedFieldCellEditor(getTextField()));
        getCellEditor().addCellEditorListener(this);
        this.validator = new MockedFieldValidator();
        this.fieldsEditorContext = fieldsEditorContext;
        addMouseListener(new PopupMenuManager());
        initCellRenderer();
    }

    private void initCellRenderer() {
        DefaultTreeCellRenderer cellRenderer = new DefaultTreeCellRenderer();
        cellRenderer.setLeafIcon(getIcon("propertieseditor.tree.leaf.icon"));
        cellRenderer.setClosedIcon(getIcon("propertieseditor.tree.closed.icon"));
        cellRenderer.setOpenIcon(getIcon("propertieseditor.tree.open.icon"));
        cellRenderer.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
        setCellRenderer(cellRenderer);
    }

    @Override
    public String convertValueToText(Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
        if (!isMockedFieldNode(value)) return super.convertValueToText(value, selected, expanded, leaf, row, hasFocus);
        MockedFieldNode node = (MockedFieldNode) value;
        if (EmptyTreeNode.class.isInstance(node))
            return getMessage("main.propertieseditor.tree.novalues.text");
        if (!leaf) return node.toString();
        return node.getUserObject().getCompleteDescription();
    }

    @Override
    public void setFields(List<MockedField> fields) {
        final Enumeration<TreePath> expandedPaths = getExpandedDescendants(new TreePath(getModel().getRoot()));
        NodeFillStrategy strategy = bestStrategyFor(getRoot());
        for (MockedField field : fields) {
            strategy.insertField(getRoot(), field);
        }
        getModel().reload();
        while (expandedPaths != null && expandedPaths.hasMoreElements()) {
            expandPath(expandedPaths.nextElement());
        }
    }

    @Override
    public DefaultTreeModel getModel() {
        return (DefaultTreeModel) super.getModel();
    }

    private MockedFieldNode getRoot() {
        return (MockedFieldNode) getModel().getRoot();
    }

    @Override
    public void editFieldAt(Point point) {
        MockedField field = getFieldAt(point);
        if (field.isSimpleValue()) startEditingAtPath(getPathForLocation(point.x, point.y));
        else fireEditingStarted(field, point);
    }

    private void fireEditingStarted(MockedField field, Point point) {
        MockedFieldEditingEvent event = new MockedFieldEditingEvent(this, field, fieldsEditorContext, point);
        for (FieldEditingListener editingListener : editingListeners) {
            editingListener.editingStarted(event);
        }
    }

    @Override
    public void selectFieldAt(Point point) {
        TreePath path = getPathForLocation(point.x, point.y);
        if (path != null) setSelectionPath(path);
    }

    @Override
    public List<MockedField> getSelectedFields() {
        return Arrays.asList(getSelectedField());
    }

    @Override
    public void addFieldEditingListener(FieldEditingListener fieldEditingListener) {
        editingListeners.add(fieldEditingListener);
    }

    @Override
    public void removeFieldEditingListener(FieldEditingListener fieldEditingListener) {
        editingListeners.remove(fieldEditingListener);
    }

    @Override
    public void fieldsChanged(List<MockedField> fields) {
        NodeFillStrategy strategy = bestStrategyFor(getRoot());
        MockedFieldNode root = getRoot();
        for (MockedField field : fields) {
            MockedFieldNode parent = applyStrategy(strategy, root, field);
            getModel().reload(parent);
        }
    }

    @Override
    public void contextInstalled(String contextPath, List<MockedField> fields) {
        fieldsChanged(fields);
    }

    @Override
    public void contextRemoved(String contextPath, List<MockedField> fields) {
        MockedFieldNode root = ((MockedFieldNode) getModel().getRoot());
        MockedFieldNode deleted = null;
        Enumeration<?> e = root.children();
        boolean found = false;
        while (!found && e.hasMoreElements()) {
            MockedFieldNode current = (MockedFieldNode) e.nextElement();
            found = contextPath.equals(current.getUserObject().getContextPath());
            if (found) deleted = current;
        }
        if (deleted != null) deleted.removeFromParent();
    }


    @Override
    public boolean isPathEditable(TreePath path) {
        if (!isEditable() || !getModel().isLeaf(path.getLastPathComponent())) return false;
        MockedFieldNode node = (MockedFieldNode) path.getLastPathComponent();
        return node != null && node.getUserObject().isSimpleValue();
    }

    @Override
    public void editingCanceled(ChangeEvent e) {
        log.debug("editing canceled");
    }

    @Override
    public void editingStopped(ChangeEvent e) {
        TreePath editingPath = getSelectionPath();
        if (editingPath == null) return;
        MockedFieldNode node = (MockedFieldNode) editingPath.getLastPathComponent();
        if (node == null) return;
        MockedField mf = node.getUserObject();
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
        getModel().reload(node);
        if (fieldsEditorContext.isNotifyChangeNeeded()) {
            MockedFieldChanged event = new MockedFieldChanged(this, mf);
            publishApplicationEvent(event);
        }
    }

    public void redraw(int targetX, int targetY) {
        TreePath path = getPathForLocation(targetX, targetY);
        getModel().reload();
        setSelectionPath(path);
    }

    private boolean isMockedFieldNode(Object node) {
        return MockedFieldNode.class.isInstance(node);
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
    public boolean hasEditableFieldAtLocation(Point point) {
        MockedFieldNode node = getNodeAt(point.x, point.y);
        return node != null && node.isLeaf();
    }

    @Override
    public MockedField getFieldAt(Point point) {
        return getFieldAt(point.x, point.y);
    }

    @Override
    public MockedField getFieldAt(int x, int y) {
        MockedFieldNode node = getNodeAt(x, y);
        if (node == null) return null;
        return node.getUserObject();
    }

    private MockedFieldNode getNodeAt(int x, int y) {
        TreePath path = getPathForLocation(x, y);
        if (path == null) return null;
        return (MockedFieldNode) path.getLastPathComponent();
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
            MockedFieldNode node = (MockedFieldNode) value;
            if (node.getUserObject() == null)
                return super.getTreeCellEditorComponent(tree, value, isSelected, expanded, leaf, row);
            super.delegate.setValue(node.getUserObject().getFieldValue());
            return super.editorComponent;
        }
    }
}
