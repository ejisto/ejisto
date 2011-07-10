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

import org.jdesktop.swingx.JXPanel;

import javax.swing.*;
import java.awt.*;

/**
 * Created by IntelliJ IDEA.
 * User: celestino
 * Date: 6/2/11
 * Time: 5:26 PM
 */
public class ContainerTab extends JSplitPane {

    private String containerId;
    private LogViewer logViewer;
    private JScrollPane scrollableContextList;
    private RegisteredContextList contextList;
    private JPanel serverSummaryPane;
    private String serverName;

    public ContainerTab(String serverName, String containerId) {
        super();
        this.containerId = containerId;
        this.serverName = serverName;
        setName(serverName);
        init();
    }

    public void log(String message) {
        getLogViewer().log(message);
    }

    public String getContainerId() {
        return containerId;
    }

    public void reloadApplications() {
        getContextList().reloadAllContexts();
    }

    private void init() {
        setOrientation(HORIZONTAL_SPLIT);
        initLeftComponent();
        initRightComponent();
        setDividerSize(2);
        setResizeWeight(1.0D);
    }

    private void initLeftComponent() {
        JXPanel leftComponent = new JXPanel(new BorderLayout());
        leftComponent.add(getServerSummaryPane(), BorderLayout.NORTH);
        leftComponent.add(getLogViewer(), BorderLayout.CENTER);
        leftComponent.setPreferredSize(new Dimension(500, 250));
        leftComponent.setMinimumSize(new Dimension(500, 250));
        leftComponent.setMaximumSize(new Dimension(Short.MAX_VALUE, Short.MAX_VALUE));
        setLeftComponent(leftComponent);
    }

    private void initRightComponent() {
        setRightComponent(getScrollableContextList());
    }

    private JPanel getServerSummaryPane() {
        if (this.serverSummaryPane != null) return serverSummaryPane;
        serverSummaryPane = new ServerSummary(containerId, serverName);
        return serverSummaryPane;
    }

    private LogViewer getLogViewer() {
        if (this.logViewer != null) return this.logViewer;
        logViewer = new LogViewer();
        return logViewer;
    }

    private JScrollPane getScrollableContextList() {
        if (this.scrollableContextList != null) return this.scrollableContextList;
        scrollableContextList = new JScrollPane(getContextList(), ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
                                                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollableContextList.setMinimumSize(new Dimension(250, 100));
        scrollableContextList.setPreferredSize(new Dimension(250, 300));
        scrollableContextList.setMaximumSize(new Dimension(250, Short.MAX_VALUE));
        scrollableContextList.setName(getContextList().getName());

        return scrollableContextList;
    }

    private RegisteredContextList getContextList() {
        if (this.contextList != null) return this.contextList;
        contextList = new RegisteredContextList();
        return contextList;
    }

}
