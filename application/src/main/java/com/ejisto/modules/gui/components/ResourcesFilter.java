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

import org.jdesktop.swingx.JXList;
import org.jdesktop.swingx.JXPanel;
import org.jdesktop.swingx.action.AbstractActionExt;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static com.ejisto.util.GuiUtils.getMessage;
import static java.util.Arrays.binarySearch;
import static java.util.Collections.emptyList;

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

    @SuppressWarnings("unchecked")
    public void setResources(List<String> resources) {
        Vector<String> vector = new Vector<>(resources);
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
        if (this.scrollPane != null) {
            return this.scrollPane;
        }
        scrollPane = new JScrollPane(getResourcesList(), JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                                     JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        return scrollPane;
    }

    private JXList getResourcesList() {
        if (this.resourcesList != null) {
            return this.resourcesList;
        }
        resourcesList = new JXList(true);
        resourcesList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        return resourcesList;
    }

    /**
     * only for unit testing purposes
     * @param resourcesList the component
     */
    void setResourcesList(JXList resourcesList) {
        this.resourcesList = resourcesList;
    }

    private JXPanel getButtonsPanel() {
        if (this.buttonsPanel != null) {
            return this.buttonsPanel;
        }
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
        if (selectNone == null) {
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

    public final void select(boolean all) {
        if (all) {
            getResourcesList().addSelectionInterval(0, resourcesSize - 1);
        } else {
            getResourcesList().clearSelection();
        }
    }

    public List<String> getBlacklistedObjects() {
        int[] selectedIndices = getResourcesList().getSelectedIndices();
        final int[] sortedSelectedIndices = Arrays.copyOf(selectedIndices, selectedIndices.length);
        Arrays.sort(sortedSelectedIndices);
        if (selectedIndices.length == resourcesSize) {
            return emptyList();
        }
        if (selectedIndices.length == 0) {
            return resources;
        }
        Stream<String> stream = IntStream.range(0, resourcesSize)
                .filter(i -> Arrays.binarySearch(sortedSelectedIndices, i) < 0)
                .mapToObj(resources::get);
        return stream.collect(Collectors.toList());
    }

}
