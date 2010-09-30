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

import com.ejisto.modules.dao.entities.MockedField;
import com.ejisto.modules.gui.components.helper.MockedFieldTree;
import org.jdesktop.swingx.JXPanel;
import org.jdesktop.swingx.JXTable;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.tree.TreeSelectionModel;
import java.awt.*;
import java.util.List;

import static com.ejisto.util.GuiUtils.getMessage;


public class MockedFieldsEditor extends JXPanel implements ChangeListener {
    private static final long serialVersionUID = 4090818654347648102L;
	private JXTable flattenTable;
	private JTabbedPane editorContainer;
	private JScrollPane flattenTableContainer;
	private JScrollPane treeContainer;
    private MockedFieldTree tree;
    private boolean main;
    private List<MockedField> fields;

    public MockedFieldsEditor() {
        this(false);
    }
	
	public MockedFieldsEditor(boolean main) {
		this.main=main;
	    init();
		
	}
	
	private void init() {
		setLayout(new BorderLayout());
		add(getEditorContainer(), BorderLayout.CENTER);
//		if(main) addbuttonspanel
	}
	
	private JTabbedPane getEditorContainer() {
	    if(editorContainer != null) return editorContainer;
	    editorContainer = new JTabbedPane(JTabbedPane.BOTTOM);
	    editorContainer.addTab(getMessage("wizard.properties.editor.tab.flat.text"), getFlattenTableContainer());
	    editorContainer.addTab(getMessage("wizard.properties.editor.tab.hierarchical.text"), getTreeContainer());
        editorContainer.addChangeListener(this);
	    return editorContainer;
	}
	
	private JScrollPane getTreeContainer() {
	    if(treeContainer != null) return treeContainer;
	    treeContainer = new JScrollPane(getTree(), JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
	    return treeContainer;
	}
	
	private MockedFieldTree getTree() {
	    if(this.tree != null) return this.tree;
	    tree = new MockedFieldTree(main);
	    tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
	    tree.setEditable(true);
	    tree.setExpandsSelectedPaths(true);
	    return tree;
	}
	
	private JScrollPane getFlattenTableContainer() {
        if(flattenTableContainer != null) return flattenTableContainer;
        flattenTableContainer = new JScrollPane(getFlattenTable(), JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        return flattenTableContainer;
    }
	
	private JXTable getFlattenTable() {
		if(flattenTable != null) return flattenTable;
		flattenTable = new JXTable();
		return flattenTable;
	}
	
	public void setFields(List<MockedField> fields) {
        this.fields=fields;
		refreshFlattenTableModel(fields);
		refreshTreeModel(fields);
	}

    private void refreshFlattenTableModel(List<MockedField> fields) {
        getFlattenTable().setModel(new MockedFieldsTableModel(fields, main, !main));
    }

    private void refreshTreeModel(List<MockedField> fields) {
        getTree().setFields(fields);
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        int selectedIndex = ((JTabbedPane)e.getSource()).getSelectedIndex();
        if(selectedIndex == 0) {
            refreshFlattenTableModel(fields);
        } else {
            refreshTreeModel(fields);
        }
        
    }
}
