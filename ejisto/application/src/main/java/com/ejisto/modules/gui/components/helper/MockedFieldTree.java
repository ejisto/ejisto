package com.ejisto.modules.gui.components.helper;

import static ch.lambdaj.Lambda.by;
import static ch.lambdaj.Lambda.group;
import static ch.lambdaj.Lambda.on;
import static com.ejisto.util.GuiUtils.getMessage;

import java.awt.Component;
import java.awt.Dimension;
import java.util.Collection;
import java.util.List;

import javax.swing.DefaultCellEditor;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeCellEditor;
import javax.swing.tree.TreeNode;

import com.ejisto.modules.dao.entities.MockedField;

import ch.lambdaj.group.Group;

public class MockedFieldTree extends JTree implements CellEditorListener {
    private static final long serialVersionUID = 3542351125591491996L;
    private String rootText = getMessage("wizard.properties.editor.tab.hierarchical.rootnode");
    private JTextField textField;
    
    public MockedFieldTree() {
        super();
        super.setEditable(true);
        setCellEditor(new MockedFieldCellEditor(getTextField()));
    }
    
    @Override
    public void setEditable(boolean flag) {
        //nothing to be done
    }

    private JTextField getTextField() {
        if(this.textField != null) return this.textField;
        textField = new JTextField();
        textField.setPreferredSize(new Dimension(300,20));
        return textField;
    }
    
    @Override
    public String convertValueToText(Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode)value;
        if(EmptyTreeNode.class.isAssignableFrom(node.getClass())) return getMessage("main.propertieseditor.tree.novalues.text");
        if(!MockedField.class.isAssignableFrom(node.getUserObject().getClass())) return super.convertValueToText(value, selected, expanded, leaf, row, hasFocus);
        MockedField mockedField = (MockedField)((DefaultMutableTreeNode)value).getUserObject();
        if(!leaf) return row == 0 ? rootText : mockedField.getClassName();
        return new StringBuilder(mockedField.getFieldName()).append(" [").append(mockedField.getFieldType()).append("]: ").append(mockedField.getFieldValue()).toString();
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
        if(in.isEmpty()) return new EmptyTreeNode();
        Group<MockedField> groupedFields = group(in, by(on(MockedField.class).getGroupKey()), by(on(MockedField.class).getFieldName()));
        DefaultMutableTreeNode root = group2Node(groupedFields);
        return root;
    }

    private DefaultMutableTreeNode group2Node(Group<MockedField> group) {
        DefaultMutableTreeNode node;
        if (group.isLeaf())
            node = new DefaultMutableTreeNode(group.first());
        else {
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
    public void editingStopped(ChangeEvent e) {
        TreeCellEditor editor = getCellEditor();
        DefaultMutableTreeNode node = (DefaultMutableTreeNode)getEditingPath().getLastPathComponent();
        MockedField mf = (MockedField)node.getUserObject();
        mf.setFieldValue(String.valueOf(editor.getCellEditorValue()));
        ((DefaultTreeModel)getModel()).reload(node.getParent());
    }
    
    private static final class MockedFieldCellEditor extends DefaultCellEditor {
        private static final long serialVersionUID = 8403949313719981178L;

        public MockedFieldCellEditor(JTextField textField) {
            super(textField);
        }

        @Override
        public Component getTreeCellEditorComponent(JTree tree, Object value, boolean isSelected, boolean expanded, boolean leaf, int row) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode)value;
            if(!MockedField.class.isAssignableFrom(node.getUserObject().getClass()))
                return super.getTreeCellEditorComponent(tree, value, isSelected, expanded, leaf, row);
            super.delegate.setValue(((MockedField)node.getUserObject()).getFieldValue());
            return super.editorComponent;
        }
    }
    
    private static final class EmptyTreeNode extends DefaultMutableTreeNode {
        private static final long serialVersionUID = 1L;
        
    }
}
