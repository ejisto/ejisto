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
import static java.util.Arrays.binarySearch;
import static java.util.Collections.emptyList;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Vector;

import javax.swing.Action;
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
    private Action selectAll;
    private Action selectNone;

    public ResourcesFilter() {
        this(null, null);
    }

    public ResourcesFilter(Action selectAll, Action selectNone) {
        this.selectAll = selectAll;
        this.selectNone = selectNone;
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
        if (this.scrollPane != null) return this.scrollPane;
        scrollPane = new JScrollPane(getResourcesList(), JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        return scrollPane;
    }

    private JXList getResourcesList() {
        if (this.resourcesList != null) return this.resourcesList;
        resourcesList = new JXList(true);
        resourcesList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        return resourcesList;
    }

    private JXPanel getButtonsPanel() {
        if (this.buttonsPanel != null) return this.buttonsPanel;
        buttonsPanel = new JXPanel();
        JButton bSelectAll;
        if (selectAll == null) {
            bSelectAll = new JButton(new AbstractActionExt(getMessage("wizard.jarfilter.selectall.text")) {
                private static final long serialVersionUID = 1L;

                @Override
                public void actionPerformed(ActionEvent e) {
                    select(true);
                }
            });
        } else {
            bSelectAll = new JButton(selectAll);
        }

        JButton bSelectNone;
        if(selectNone == null) {
            bSelectNone = new JButton(new AbstractActionExt(getMessage("wizard.jarfilter.selectnone.text")) {
                private static final long serialVersionUID = 1L;

                @Override
                public void actionPerformed(ActionEvent e) {
                    select(false);
                }
            }); 
        } else {
            bSelectNone = new JButton(selectNone);
        }
        buttonsPanel.add(bSelectAll);
        buttonsPanel.add(bSelectNone);
        return buttonsPanel;
    }
    
    public void select(boolean all) {
        if(all) getResourcesList().addSelectionInterval(0, resourcesSize - 1);
        else getResourcesList().clearSelection();
    }
    
    public List<String> getBlacklistedObjects() {
        int[] indices = getResourcesList().getSelectedIndices();
        if (indices.length == resourcesSize) return emptyList();
        if (indices.length == 0) return resources;
        List<String> ret = new ArrayList<String>();
        int i = 0;
        for (String jar : resources)
            if (binarySearch(indices, i++) > -1) ret.add(jar);
        return ret;
    }
    
}
