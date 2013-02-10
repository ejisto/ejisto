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

import ch.lambdaj.function.closure.Closure1;
import com.ejisto.modules.controller.wizard.installer.JndiResourcesEditorController;
import com.ejisto.modules.dao.entities.JndiDataSource;
import com.ejisto.modules.gui.components.helper.BoundResourceEditor;
import com.ejisto.modules.gui.components.helper.CallbackAction;
import com.ejisto.modules.gui.components.helper.JndiResourcesTreeModel;
import org.jdesktop.swingx.JXPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.util.List;

import static ch.lambdaj.Lambda.var;
import static com.ejisto.constants.StringConstants.SELECT_FILE_COMMAND;
import static javax.swing.JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED;
import static javax.swing.JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED;

public class JndiResourcesEditor extends JXPanel {
    private List<JndiDataSource> dataSources;
    private JTabbedPane tabbedPane;
    private JXPanel summaryPane;
    private transient JndiResourcesEditorController fileSelectionListener;
    private JndiResourcesTreeModel resourcesTreeModel;
    private JTree resourcesTree;

    public JndiResourcesEditor() {
        super();
    }

    public void init(List<JndiDataSource> dataSources) {
        this.dataSources = dataSources;
        setLayout(new BorderLayout());
        add(getResourcesTabbedPane(), BorderLayout.EAST);
        add(getSummaryPane(), BorderLayout.CENTER);
    }

    public void setFileSelectionListener(JndiResourcesEditorController fileSelectionListener) {
        this.fileSelectionListener = fileSelectionListener;
    }

    private JTabbedPane getResourcesTabbedPane() {
        if (this.tabbedPane != null) {
            return this.tabbedPane;
        }
        Closure1<ActionEvent> callActionPerformed = new Closure1<ActionEvent>() {{
            of(fileSelectionListener).actionPerformed(var(ActionEvent.class));
        }};
        tabbedPane = new JTabbedPane(JTabbedPane.BOTTOM);
        BoundResourceEditor editor;
        int index = 0;
        for (JndiDataSource dataSource : dataSources) {
            editor = new BoundResourceEditor(dataSource, this, index++);
            editor.getActionMap().put(SELECT_FILE_COMMAND.getValue(),
                                      new CallbackAction(SELECT_FILE_COMMAND.getValue(), callActionPerformed));
            tabbedPane.add(editor.getBoundResourceEditor());
        }
        return tabbedPane;
    }

    private JXPanel getSummaryPane() {
        if (this.summaryPane != null) {
            return this.summaryPane;
        }
        summaryPane = new JXPanel(new BorderLayout());
        summaryPane.add(getResourcesScrollPane(), BorderLayout.CENTER);
        return summaryPane;
    }

    private JScrollPane getResourcesScrollPane() {
        return new JScrollPane(getResourcesTree(), VERTICAL_SCROLLBAR_AS_NEEDED, HORIZONTAL_SCROLLBAR_AS_NEEDED);
    }

    private JTree getResourcesTree() {
        if (this.resourcesTree != null) {
            return this.resourcesTree;
        }
        resourcesTree = new JTree(getResourcesTreeModel()) {
            @Override
            public String getToolTipText(MouseEvent event) {
                if (getRowForLocation(event.getX(), event.getY()) != -1) {
                    return getPathForLocation(event.getX(), event.getY()).getLastPathComponent().toString();
                }
                return super.getToolTipText(event);
            }
        };
        ToolTipManager.sharedInstance().registerComponent(resourcesTree);
        return resourcesTree;
    }

    private JndiResourcesTreeModel getResourcesTreeModel() {
        if (this.resourcesTreeModel != null) {
            return this.resourcesTreeModel;
        }
        resourcesTreeModel = new JndiResourcesTreeModel(dataSources);
        return resourcesTreeModel;
    }

    public void reloadElement(int index) {
        getResourcesTreeModel().buildTree();
        getResourcesTreeModel().reload(index);
        getResourcesTree().expandPath(getResourcesTreeModel().getPathForChild(index));
    }

    public List<JndiDataSource> getDataSources() {
        return dataSources;
    }
}
