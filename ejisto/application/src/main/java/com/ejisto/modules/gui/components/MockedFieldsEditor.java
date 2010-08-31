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
	
	public MockedFieldsEditor() {
        init();
    }
	
	private void init() {
		setLayout(new BorderLayout());
		add(getEditorContainer(), BorderLayout.CENTER);
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
		getFlattenTable().setModel(new MockedFieldsTableModel(fields, false, true));
		getTree().setFields(fields);
	}
	
}
