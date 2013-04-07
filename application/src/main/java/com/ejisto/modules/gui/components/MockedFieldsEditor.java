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

import com.ejisto.event.ApplicationListener;
import com.ejisto.event.def.MockedFieldChanged;
import com.ejisto.event.ApplicationEventDispatcher;
import com.ejisto.modules.controller.MockedFieldsEditorController;
import com.ejisto.modules.dao.entities.MockedField;
import com.ejisto.modules.gui.components.helper.EditorType;
import com.ejisto.modules.gui.components.helper.FieldEditingListener;
import com.ejisto.modules.gui.components.helper.FieldEditorPanel;
import com.ejisto.modules.gui.components.helper.FieldsEditorContext;
import com.ejisto.util.GuiUtils;
import org.jdesktop.swingx.JXPanel;
import org.jdesktop.swingx.JXRadioGroup;

import javax.swing.*;
import javax.swing.tree.TreeSelectionModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseListener;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static com.ejisto.util.GuiUtils.*;

public class MockedFieldsEditor extends JXPanel implements ItemListener {
    private static final long serialVersionUID = 4090818654347648102L;

    private MockedFieldTable flattenTable;
    private JPanel editorContainer;
    private JScrollPane flattenTableContainer;
    private JPanel treeEditor;
    private JScrollPane treeContainer;
    private FieldEditorPanel valueEditorPanel;
    private MockedFieldTree tree;
    private JPanel editorSelectionPanel;
    private JPanel editorPanel;
    private transient MockedFieldsEditorController controller;
    private final FieldsEditorContext fieldsEditorContext;
    private final ApplicationEventDispatcher eventDispatcher;

    public MockedFieldsEditor(FieldsEditorContext fieldsEditorContext,
                              ApplicationEventDispatcher eventDispatcher,
                              ActionMap actionMap) {
        this.fieldsEditorContext = fieldsEditorContext;
        this.eventDispatcher = eventDispatcher;
        getActionMap().setParent(actionMap);
        init();
        initActionMap(actionMap);
    }

    public void setFields(List<MockedField> fields) {
        for (EditorType editorType : fieldsEditorContext.getSupportedEditors()) {
            getEditorComponent(editorType).setFields(fields);
        }
    }

    public void contextInstalled(String contextPath, List<MockedField> fields) {
        for (EditorType editorType : fieldsEditorContext.getSupportedEditors()) {
            getEditorComponent(editorType).contextInstalled(contextPath, fields);
        }
    }

    public void contextRemoved(String contextPath, List<MockedField> fields) {
        for (EditorType editorType : fieldsEditorContext.getSupportedEditors()) {
            getEditorComponent(editorType).contextRemoved(contextPath, fields);
        }
    }

    public void initActionMap(ActionMap actionMap) {
        GuiUtils.setActionMap(actionMap, getTree());
        GuiUtils.setActionMap(actionMap, getFlattenTable());
    }

    public String getExpression() {
        return getValueEditorPanel().getExpression();
    }

    public String getFieldSize() {
        return getValueEditorPanel().getFieldSize();
    }

    public String getFieldType() {
        return getValueEditorPanel().getFieldClass();
    }

    public MockedField getMockedFieldAt(int x, int y, boolean fromTree) {
        MockedFieldsEditorComponent component;
        if (fromTree) {
            component = getTree();
        } else {
            component = getFlattenTable();
        }
        return component.getFieldAt(x, y);
    }

    public void registerChangeListener(MockedFieldsEditorController controller) {
        ((JXRadioGroup<?>) getEditorSelectionPanel()).addActionListener(controller);
        this.controller = controller;
    }

    public void registerMouseLister(MouseListener mouseListener) {
        for (EditorType editorType : fieldsEditorContext.getSupportedEditors()) {
            getEditorComponent(editorType).addMouseListener(mouseListener);
        }
    }

    public void expandCollapseEditorPanel(boolean expand) {
        getValueEditorPanel().setCollapsed(!expand);
    }

    public void setFocusOnComplexEditorPanel() {
        getValueEditorPanel().setFocusOnFirstField();
    }

    public void initEditorPanel(Collection<String> types, String title, MockedField editedField) {
        if (editedField.getFieldElementType() != null) {
            getValueEditorPanel().setTypes(Arrays.asList(editedField.getFieldElementType()));
        } else {
            getValueEditorPanel().setTypes(types);
        }
        getValueEditorPanel().setTitle(title);
    }

    private void init() {
        setName(getMessage("main.propertieseditor.title.text"));
        setLayout(new BorderLayout());
        add(getEditorContainer(), BorderLayout.CENTER);
        eventDispatcher.registerApplicationEventListener(MockedFieldChanged.class,
                                                         new ApplicationListener<MockedFieldChanged>() {
                                                             @Override
                                                             public void onApplicationEvent(final MockedFieldChanged event) {
                                                                 runOnEDT(new Runnable() {
                                                                     @Override
                                                                     public void run() {
                                                                         fieldsChanged(event.getMockedFields());
                                                                     }
                                                                 });
                                                             }
                                                         });
    }

    private void fieldsChanged(List<MockedField> fields) {
        for (EditorType editorType : fieldsEditorContext.getSupportedEditors()) {
            getEditorComponent(editorType).fieldsChanged(fields);
        }
    }

    private JPanel getEditorContainer() {
        if (editorContainer != null) {
            return editorContainer;
        }
        editorContainer = new JXPanel(new BorderLayout());
        editorContainer.add(getEditorSelectionPanel(), BorderLayout.NORTH);
        editorContainer.add(getEditorPanel(), BorderLayout.CENTER);
        editorContainer.add(getValueEditorPanel(), BorderLayout.SOUTH);
        return editorContainer;
    }

    private JPanel getEditorPanel() {
        if (this.editorPanel != null) {
            return this.editorPanel;
        }
        CardLayout layout = new CardLayout();
        editorPanel = new JXPanel(layout);
        for (EditorType editorType : fieldsEditorContext.getSupportedEditors()) {
            editorPanel.add(getEditor(editorType), editorType.getLabel());
        }
        return editorPanel;
    }

    private Component getEditor(EditorType editorType) {
        switch (editorType) {
            case HIERARCHICAL:
                return getTreeEditor();
            case FLATTEN:
                return getFlattenTableContainer();
            default:
                throw new IllegalArgumentException(editorType.name());
        }
    }

    private MockedFieldsEditorComponent getEditorComponent(EditorType editorType) {
        switch (editorType) {
            case HIERARCHICAL:
                return getTree();
            case FLATTEN:
                return getFlattenTable();
            default:
                throw new IllegalArgumentException(editorType.name());
        }
    }

    private JPanel getEditorSelectionPanel() {
        if (this.editorSelectionPanel != null) {
            return this.editorSelectionPanel;
        }
        editorSelectionPanel = JXRadioGroup.create(createEditorTypeButtons());
        editorSelectionPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        return editorSelectionPanel;
    }

    private Component[] createEditorTypeButtons() {
        Collection<EditorType> supportedEditorTypes = fieldsEditorContext.getSupportedEditors();
        if (supportedEditorTypes.size() < 2) {
            return new Component[0];
        }
        Component[] buttons = new Component[supportedEditorTypes.size()];
        int index = 0;
        for (EditorType editorType : supportedEditorTypes) {
            buttons[index] = createButton(editorType, index == 0);
            index++;
        }
        return buttons;
    }

    private AbstractButton createButton(EditorType option, boolean selected) {
        JRadioButton radioButton = new JRadioButton(option.getLabel(), selected);
        radioButton.setActionCommand(option.toString());
        radioButton.addItemListener(this);
        radioButton.setBackground(null);
        radioButton.setFocusPainted(false);
        return radioButton;
    }

    private JPanel getTreeEditor() {
        if (this.treeEditor != null) {
            return this.treeEditor;
        }
        treeEditor = new JXPanel(new BorderLayout(2, 0));
        treeEditor.add(getTreeContainer(), BorderLayout.CENTER);
        return treeEditor;
    }

    private FieldEditorPanel getValueEditorPanel() {
        if (this.valueEditorPanel != null) {
            return valueEditorPanel;
        }
        valueEditorPanel = new FieldEditorPanel(getActionMap(), fieldsEditorContext);
        valueEditorPanel.setCollapsed(true);
        valueEditorPanel.setPreferredSize(new Dimension(200, 150));
        return valueEditorPanel;
    }

    private JScrollPane getTreeContainer() {
        if (treeContainer != null) {
            return treeContainer;
        }
        treeContainer = new JScrollPane(getTree(), JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                                        JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        return treeContainer;
    }

    public MockedFieldTree getTree() {
        if (this.tree != null) {
            return this.tree;
        }
        tree = new MockedFieldTree(fieldsEditorContext);
        tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        tree.setEditable(true);
        tree.setExpandsSelectedPaths(true);
        return tree;
    }

    private JScrollPane getFlattenTableContainer() {
        if (flattenTableContainer != null) {
            return flattenTableContainer;
        }
        flattenTableContainer = new JScrollPane(getFlattenTable(), JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                                                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        return flattenTableContainer;
    }

    private MockedFieldTable getFlattenTable() {
        if (flattenTable != null) {
            return flattenTable;
        }
        flattenTable = new MockedFieldTable(fieldsEditorContext);
        return flattenTable;
    }

    public void showCard(EditorType editorType) {
        ((CardLayout) getEditorPanel().getLayout()).show(getEditorPanel(), editorType.toString());
    }

    @Override
    public void itemStateChanged(ItemEvent e) {
        if (e.getStateChange() == ItemEvent.SELECTED) {
            ActionEvent event = new ActionEvent(this, e.getID(), ((AbstractButton) e.getSource()).getActionCommand());
            controller.actionPerformed(event);
        }
    }

    public List<MockedField> getTreeSelectedItems() {
        return getTree().getSelectedFields();
    }

    public List<MockedField> getTableSelectedItems() {
        return getFlattenTable().getSelectedFields();
    }

    public void registerFieldEditingListener(FieldEditingListener listener) {
        for (EditorType editorType : fieldsEditorContext.getSupportedEditors()) {
            getEditorComponent(editorType).addFieldEditingListener(listener);
        }
    }

    public void requestFocusOnActiveEditor(EditorType editorType) {
        getEditorComponent(editorType).toComponent().requestFocus();
    }
}
