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

import com.ejisto.modules.controller.MockedFieldsEditorController;
import com.ejisto.modules.dao.entities.MockedField;
import com.ejisto.modules.gui.components.helper.MockedFieldValueEditorPanel;
import org.jdesktop.swingx.JXPanel;
import org.jdesktop.swingx.JXTable;

import javax.swing.*;
import javax.swing.tree.TreeSelectionModel;
import java.awt.*;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static com.ejisto.util.GuiUtils.getMessage;

public class MockedFieldsEditor extends JXPanel {
    private static final long serialVersionUID = 4090818654347648102L;
    private JXTable flattenTable;
    private JTabbedPane editorContainer;
    private JScrollPane flattenTableContainer;
    private JPanel treeEditor;
    private JScrollPane treeContainer;
    private MockedFieldValueEditorPanel valueEditorPanel;
    private MockedFieldTree tree;
    private boolean main;
    private List<MockedField> fields;

    public MockedFieldsEditor(boolean main) {
        this.main = main;
        init();
    }

    public void setFields(List<MockedField> fields) {
        this.fields = fields;
        refreshFlattenTableModel();
        refreshTreeModel();
    }

    public void initActionMap(ActionMap actionMap) {
        getActionMap().setParent(actionMap);
        getTree().getActionMap().setParent(actionMap);
        getValueEditorPanel().getActionMap().setParent(actionMap);
        getFlattenTable().getActionMap().setParent(actionMap);
    }

    public String getExpression() {
        return getValueEditorPanel().getExpression();
    }

    public String getFieldSize() {
        return getValueEditorPanel().getFieldSize();
    }

    public String getFieldType() {
        return getValueEditorPanel().getFieldType();
    }

    public void refreshFlattenTableModel() {
        getFlattenTable().setModel(new MockedFieldsTableModel(fields, main, !main));
    }

    public void refreshTreeModel() {
        getTree().setFields(fields);
    }

    public void registerChangeListener(MockedFieldsEditorController controller) {
        getEditorContainer().addChangeListener(controller);
        getTree().addMouseListener(controller);
    }

    public void expandCollapseEditorPanel(boolean expand) {
        getValueEditorPanel().setCollapsed(!expand);
    }

    public void initEditorPanel(Collection<String> types, String title) {
        MockedField field = getTree().getSelectedField();
        if (field.getFieldElementType() != null)
            getValueEditorPanel().setTypes(Arrays.asList(field.getFieldElementType()));
        else getValueEditorPanel().setTypes(types);
        getValueEditorPanel().setTitle(title);
    }

    private void init() {
        setLayout(new BorderLayout());
        add(getEditorContainer(), BorderLayout.CENTER);
    }

    private JTabbedPane getEditorContainer() {
        if (editorContainer != null) return editorContainer;
        editorContainer = new JTabbedPane(JTabbedPane.BOTTOM);
        editorContainer.addTab(getMessage("wizard.properties.editor.tab.hierarchical.text"), getTreeEditor());
        editorContainer.addTab(getMessage("wizard.properties.editor.tab.flat.text"), getFlattenTableContainer());
        return editorContainer;
    }

    private JPanel getTreeEditor() {
        if (this.treeEditor != null) return this.treeEditor;
        treeEditor = new JXPanel(new BorderLayout(2, 0));
        treeEditor.add(getTreeContainer(), BorderLayout.CENTER);
        treeEditor.add(getValueEditorPanel(), BorderLayout.SOUTH);
        return treeEditor;
    }

    private MockedFieldValueEditorPanel getValueEditorPanel() {
        if (this.valueEditorPanel != null) return valueEditorPanel;
        valueEditorPanel = new MockedFieldValueEditorPanel();
        valueEditorPanel.setCollapsed(true);
        valueEditorPanel.setPreferredSize(new Dimension(200, 150));
        return valueEditorPanel;
    }

    private JScrollPane getTreeContainer() {
        if (treeContainer != null) return treeContainer;
        treeContainer = new JScrollPane(getTree(), JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                                        JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        return treeContainer;
    }

    public MockedFieldTree getTree() {
        if (this.tree != null) return this.tree;
        tree = new MockedFieldTree(main);
        tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        tree.setEditable(true);
        tree.setExpandsSelectedPaths(true);
        return tree;
    }

    private JScrollPane getFlattenTableContainer() {
        if (flattenTableContainer != null) return flattenTableContainer;
        flattenTableContainer = new JScrollPane(getFlattenTable(), JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                                                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        return flattenTableContainer;
    }

    private JXTable getFlattenTable() {
        if (flattenTable != null) return flattenTable;
        flattenTable = new JXTable();
        return flattenTable;
    }

}
