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

import com.ejisto.event.def.MockedFieldCreated;
import com.ejisto.event.def.MockedFieldUpdated;
import com.ejisto.event.def.StatusBarMessage;
import com.ejisto.modules.dao.entities.MockedField;
import com.ejisto.modules.gui.components.helper.FieldEditingListener;
import com.ejisto.modules.gui.components.helper.FieldsEditorContext;
import com.ejisto.modules.gui.components.helper.MockedFieldEditingEvent;
import com.ejisto.modules.gui.components.helper.PopupMenuManager;
import com.ejisto.modules.gui.components.tree.NodeFillStrategy;
import com.ejisto.modules.gui.components.tree.node.ClassNode;
import com.ejisto.modules.gui.components.tree.node.FieldNode;
import com.ejisto.modules.gui.components.tree.node.RootNode;
import com.ejisto.modules.validation.MockedFieldValidator;
import com.ejisto.util.GuiUtils;
import lombok.extern.log4j.Log4j;

import javax.swing.*;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.text.Position;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.*;
import java.util.List;

import static com.ejisto.modules.gui.components.tree.FillStrategies.applyStrategy;
import static com.ejisto.modules.gui.components.tree.FillStrategies.bestStrategyFor;
import static com.ejisto.util.GuiUtils.*;

@Log4j
public class MockedFieldTree extends JTree implements CellEditorListener, MockedFieldsEditorComponent {
    private static final long serialVersionUID = 3542351125591491996L;
    private static final int EXPAND_ALL_LIMIT = 20;
    private transient MockedFieldValidator validator;
    private JTextField textField;
    private final FieldsEditorContext fieldsEditorContext;
    private final List<FieldEditingListener> editingListeners = new ArrayList<>();

    public MockedFieldTree(FieldsEditorContext fieldsEditorContext) {
        super(new RootNode());
        setCellEditor(new MockedFieldCellEditor(getTextField()));
        getCellEditor().addCellEditorListener(this);
        this.validator = new MockedFieldValidator();
        this.fieldsEditorContext = fieldsEditorContext;
        addMouseListener(new PopupMenuManager());
        initCellRenderer();
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                e.consume();
            }
        });
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
        if (!isMockedFieldNode(value)) {
            return super.convertValueToText(value, selected, expanded, leaf, row, hasFocus);
        }
        FieldNode node = (FieldNode) value;
        if (!leaf) {
            return node.toString();
        } else if (node.isRoot()) {
            return node.toString();
        }
        return node.getUserObject().getCompleteDescription();
    }

    @Override
    public TreePath getNextMatch(String prefix, int startingRow, Position.Bias bias) {
        ClassNode searchRoot;
        FieldNode node = (FieldNode) getPathForRow(startingRow).getLastPathComponent();
        if (node instanceof ClassNode) {
            searchRoot = (ClassNode) node;
        } else {
            searchRoot = (ClassNode) node.getParent();
        }
        FieldNode match = searchRoot.findMatchingNode(prefix, bias);
        if (match == null) {
            return null;
        }
        TreePath matchingPath = getNodePath(match);
        setExpandedState(matchingPath, true);
        return matchingPath;
    }

    @Override
    public void setFields(List<MockedField> fields) {
        final Enumeration<TreePath> expandedPaths = getExpandedDescendants(new TreePath(getModel().getRoot()));
        fieldsChanged(fields);
        while (expandedPaths != null && expandedPaths.hasMoreElements()) {
            expandPath(expandedPaths.nextElement());
        }
    }

    @Override
    public DefaultTreeModel getModel() {
        return (DefaultTreeModel) super.getModel();
    }

    private RootNode getRoot() {
        return (RootNode) getModel().getRoot();
    }

    @Override
    public void editFieldAt(Point point) {
        MockedField field = getFieldAt(point);
        if (field.isSimpleValue()) {
            startEditingAtPath(getPathForLocation(point.x, point.y));
        } else {
            fireEditingStarted(field, point);
        }
    }

    private void fireEditingStarted(MockedField field, Point point) {
        MockedFieldEditingEvent event = new MockedFieldEditingEvent(this, field, fieldsEditorContext, point);
        editingListeners.forEach(l -> l.editingStarted(event));
    }

    @Override
    public void selectFieldAt(Point point) {
        TreePath path = getPathForLocation(point.x, point.y);
        if (path != null) {
            setSelectionPath(path);
        }
    }

    @Override
    public List<MockedField> getSelectedFields() {
        final Optional<MockedField> selectedField = getSelectedField();
        if(selectedField.isPresent()) {
            return Arrays.asList(selectedField.get());
        }
        return Collections.emptyList();
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
    public void fieldsAdded(List<MockedField> fields) {
        RootNode root = getRoot();
        fields.forEach(f -> bestStrategyFor(root).insertField(root, f));
        getModel().reload();
    }

    @Override
    public void fieldsUpdated(List<MockedField> fields) {
        fieldsChanged(fields);
    }

    @Override
    public void fieldsRemoved(List<MockedField> fields) {
        fieldsChanged(fields);
    }

    private void fieldsChanged(List<MockedField> fields) {
        RootNode root = getRoot();
        NodeFillStrategy strategy = bestStrategyFor(root);
        fields.forEach(f -> applyStrategy(strategy, root, f));
        getModel().reload();
    }

    @Override
    public void contextInstalled(String contextPath, List<MockedField> fields) {
        getRoot().getChildrenAsStream()
                .filter(f -> f.getNodePath()[0].equals(contextPath))
                .findFirst()
                .ifPresent(x -> getRoot().remove(x));
        fieldsChanged(fields);
    }

    @Override
    public void contextRemoved(String contextPath, List<MockedField> fields) {
        getRoot().getChildrenAsStream()
                .filter(x -> contextPath.equals(x.getUserObject().getContextPath()))
                .findFirst().ifPresent(FieldNode::removeFromParent);
    }

    @Override
    public boolean fillWithCustomMenuItems(JPopupMenu menu, Point sourcePosition) {
        final FieldNode node = getNodeAt(sourcePosition.x, sourcePosition.y);
        if (node == null || node.isLeaf() || node.isEmpty()) {
            return false;
        }
        Action customAction = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                boolean expand = e.getActionCommand().equals("expand");
                toggleExpandCollapse(node, expand);
            }
        };

        boolean expanded = isExpanded(getPathForLocation(sourcePosition.x, sourcePosition.y));
        customAction.putValue(Action.NAME, getMessage(expanded ? "collapse.node.text" : "expand.node.text"));
        customAction.putValue(Action.SMALL_ICON, getIcon(expanded ? "collapse.node.icon" : "expand.node.icon"));
        customAction.putValue(Action.SHORT_DESCRIPTION,
                              getMessage(expanded ? "collapse.node.text" : "expand.node.text"));
        customAction.putValue(Action.ACTION_COMMAND_KEY, expanded ? "collapse" : "expand");
        menu.add(customAction);
        return true;
    }

    @Override
    public Component toComponent() {
        return this;
    }

    @Override
    public FieldsEditorContext getCurrentEditorContext() {
        return fieldsEditorContext;
    }


    @Override
    public boolean isPathEditable(TreePath path) {
        if (!isEditable() || !getModel().isLeaf(path.getLastPathComponent())) {
            return false;
        }
        FieldNode node = (FieldNode) path.getLastPathComponent();
        return node != null && node.getUserObject().isSimpleValue();
    }

    @Override
    public void editingCanceled(ChangeEvent e) {
        log.debug("editing canceled");
    }

    @Override
    public void editingStopped(ChangeEvent e) {
        TreePath editingPath = getSelectionPath();
        if (editingPath == null) {
            return;
        }
        FieldNode node = (FieldNode) editingPath.getLastPathComponent();
        if (node == null) {
            return;
        }
        MockedField mf = node.getUserObject();
        String previousType = mf.getFieldType();
        String previousValue = mf.getFieldValue();
        mf.setFieldValue(String.valueOf(getCellEditor().getCellEditorValue()));
        if (!validator.validate(mf)) {
            mf.setFieldValue(previousValue);
            mf.setFieldType(previousType);
            GuiUtils.publishEvent(new StatusBarMessage(this,
                                                       getMessage("propertieseditor.invalid.input",
                                                                  String.valueOf(cellEditor.getCellEditorValue()),
                                                                  mf.getFieldName()), true));
        }
        getModel().reload(node);
        if (fieldsEditorContext.isNotifyChangeNeeded()) {
            MockedFieldUpdated event = new MockedFieldUpdated(this, mf);
            GuiUtils.publishEvent(event);
        }
    }

    public void redraw(int targetX, int targetY) {
        TreePath path = getPathForLocation(targetX, targetY);
        getModel().reload();
        setSelectionPath(path);
    }

    private void toggleExpandCollapse(FieldNode parent, boolean expand) {
        if (parent.isLeaf()) {
            return;
        }
        changeNodeState(parent, expand);
        if (!expand || getModel().getChildCount(parent) > EXPAND_ALL_LIMIT) {
            return;
        }
        @SuppressWarnings("unchecked")
        Enumeration<FieldNode> e = parent.children();
        while (e.hasMoreElements()) {
            FieldNode node = e.nextElement();
            changeNodeState(node, true);
            toggleExpandCollapse(node, true);
        }
    }

    private void changeNodeState(FieldNode node, boolean expand) {
        if (node.isLeaf()) {
            return;
        }
        TreePath path = new TreePath(getModel().getPathToRoot(node));
        if (expand) {
            expandPath(path);
        } else {
            collapsePath(path);
        }
    }

    private boolean isMockedFieldNode(Object node) {
        return FieldNode.class.isInstance(node);
    }


    Optional<MockedField> getSelectedField() {
        Optional<TreePath> path = Optional.ofNullable(getSelectionPath());
        if(!path.isPresent()) {
            return Optional.empty();
        }
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.get().getLastPathComponent();
        return Optional.of((MockedField) node.getUserObject());
    }

    private JTextField getTextField() {
        if (this.textField != null) {
            return this.textField;
        }
        textField = new JTextField();
        textField.setPreferredSize(new Dimension(300, 20));
        return textField;
    }

    @Override
    public boolean hasEditableFieldAtLocation(Point point) {
        if (!fieldsEditorContext.isEditable()) {
            return false;
        }
        FieldNode node = getNodeAt(point.x, point.y);
        return node != null && node.isLeaf();
    }

    @Override
    public MockedField getFieldAt(Point point) {
        return getFieldAt(point.x, point.y);
    }

    @Override
    public MockedField getFieldAt(int x, int y) {
        FieldNode node = getNodeAt(x, y);
        if (node == null) {
            return null;
        }
        return node.getUserObject();
    }

    private FieldNode getNodeAt(int x, int y) {
        TreePath path = getPathForLocation(x, y);
        if (path == null) {
            return null;
        }
        return (FieldNode) path.getLastPathComponent();
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
            FieldNode node = (FieldNode) value;
            if (node.getUserObject() == null) {
                return super.getTreeCellEditorComponent(tree, value, isSelected, expanded, leaf, row);
            }
            super.delegate.setValue(node.getUserObject().getFieldValue());
            return super.editorComponent;
        }
    }
}
