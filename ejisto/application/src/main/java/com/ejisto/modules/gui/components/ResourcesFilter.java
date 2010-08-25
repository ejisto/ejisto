package com.ejisto.modules.gui.components;

import static com.ejisto.util.GuiUtils.getMessage;
import static java.util.Arrays.binarySearch;
import static java.util.Collections.emptyList;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;

import org.jdesktop.swingx.JXList;
import org.jdesktop.swingx.JXPanel;
import org.jdesktop.swingx.action.AbstractActionExt;

public class ResourcesFilter extends JXPanel {
    private static final long serialVersionUID = -2572933438524315729L;
    private JXList resourcesList;
    private JXPanel buttonsPanel;
    private int resourcesSize;
    private List<String> resources;
    private JScrollPane scrollPane;

    public ResourcesFilter() {
        init();
    }

    public void setResources(List<String> resources) {
        Vector<String> vector = new Vector<String>(resources);
        Collections.sort(vector);
        getResourcesList().setListData(vector);
        this.resourcesSize = vector.size();
        this.resources = vector;
    }

    private void init() {
        setLayout(new BorderLayout());
        add(getScrollPane(), BorderLayout.CENTER);
        add(getButtonsPanel(), BorderLayout.SOUTH);
    }
    
    private JScrollPane getScrollPane() {
        if(this.scrollPane != null) return this.scrollPane;
        scrollPane = new JScrollPane(getResourcesList(), JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        return scrollPane;
    }

    private JXList getResourcesList() {
        if (this.resourcesList != null)
            return this.resourcesList;
        resourcesList = new JXList(true);
        resourcesList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        return resourcesList;
    }

    private JXPanel getButtonsPanel() {
        if (this.buttonsPanel != null)
            return this.buttonsPanel;
        buttonsPanel = new JXPanel();
        JButton bSelectAll = new JButton(new AbstractActionExt(getMessage("wizard.jarfilter.selectall.text")) {
            private static final long serialVersionUID = 1L;
            @Override
            public void actionPerformed(ActionEvent e) {
                getResourcesList().addSelectionInterval(0, resourcesSize - 1);
            }
        });
        JButton bSelectNone = new JButton(new AbstractActionExt(getMessage("wizard.jarfilter.selectnone.text")) {
            private static final long serialVersionUID = 1L;
            @Override
            public void actionPerformed(ActionEvent e) {
                getResourcesList().clearSelection();
            }
        });
        buttonsPanel.add(bSelectAll);
        buttonsPanel.add(bSelectNone);
        return buttonsPanel;
    }

    public List<String> getBlacklistedObjects() {
        int[] indices = getResourcesList().getSelectedIndices();
        if (indices.length == resourcesSize)
            return emptyList();
        if (indices.length == 0)
            return resources;
        List<String> ret = new ArrayList<String>();
        int i = 0;
        for (String jar : resources)
            if (binarySearch(indices, i++) > -1)
                ret.add(jar);
        return ret;
    }

}
