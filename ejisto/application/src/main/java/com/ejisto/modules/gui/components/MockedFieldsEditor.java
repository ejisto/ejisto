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

import java.awt.BorderLayout;
import java.util.List;

import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.tree.TreeSelectionModel;

import org.jdesktop.swingx.JXPanel;
import org.jdesktop.swingx.JXTable;

import com.ejisto.modules.dao.entities.MockedField;
import com.ejisto.modules.gui.components.helper.MockedFieldTree;


public class MockedFieldsEditor extends JXPanel {
    private static final long serialVersionUID = 4090818654347648102L;
	private JXTable flattenTable;
	private JTabbedPane editorContainer;
	private JScrollPane flattenTableContainer;
	private JScrollPane treeContainer;
    private MockedFieldTree tree;
    private boolean main;
	
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
	    return editorContainer;
	}
	
	private JScrollPane getTreeContainer() {
	    if(treeContainer != null) return treeContainer;
	    treeContainer = new JScrollPane(getTree(), JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
	    return treeContainer;
	}
	
	private MockedFieldTree getTree() {
	    if(this.tree != null) return this.tree;
	    tree = new MockedFieldTree();
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
		getFlattenTable().setModel(new MockedFieldsTableModel(fields, main, !main));
		getTree().setFields(fields);
	}
	
}
