package com.ejisto.modules.gui.components;

import static com.ejisto.util.SpringBridge.getAllMockedFields;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;

import javax.swing.JButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

import org.jdesktop.swingx.JXPanel;

public class PropertyEditor extends JXPanel implements TableModelListener {
    private static final long serialVersionUID = 1619044790610314913L;
    private JScrollPane table;
    private JXPanel buttons;

    public PropertyEditor() {
        initLayout();
        initComponents();
    }

    private void initComponents() {
        add(getTable(), BorderLayout.CENTER);
        add(getButtonsBar(), BorderLayout.SOUTH);
    }
    
    private JScrollPane getTable() {
        if(table != null) return table;
        MockedFieldsTableModel tm = new MockedFieldsTableModel(getAllMockedFields());
        JTable data = new JTable(tm);
        table = new JScrollPane(data, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        return table;
    }
    
    private JXPanel getButtonsBar() {
        if(buttons != null) return buttons;
        buttons = new JXPanel();
        buttons.setLayout(new FlowLayout());
        buttons.setMinimumSize(new Dimension(200,30));
        buttons.setMaximumSize(new Dimension(Short.MAX_VALUE,30));
        buttons.setPreferredSize(new Dimension(200,30));
        buttons.add(new JButton("Insert"));
        buttons.add(new JButton("Update"));
        buttons.add(new JButton("Delete"));
        return buttons;
    }

    private void initLayout() {
        setLayout(new BorderLayout(10,10));
    }

    @Override
    public void tableChanged(TableModelEvent e) {
         
    } 
    
    
    
}
