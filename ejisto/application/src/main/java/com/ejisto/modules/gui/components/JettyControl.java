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

import com.ejisto.constants.StringConstants;
import org.jdesktop.swingx.JXCollapsiblePane;
import org.jdesktop.swingx.JXPanel;
import org.jdesktop.swingx.JXTitledPanel;

import javax.swing.*;
import java.awt.*;

import static com.ejisto.util.GuiUtils.getAction;
import static com.ejisto.util.GuiUtils.getMessage;

public class JettyControl extends JXTitledPanel {
    private static final long serialVersionUID = 4414922964189248078L;
    private JXPanel toolbarPanel;
    private JButton expandCollapseButton;
    private JXCollapsiblePane collapsibleLogPane;
    private LogViewer logViewer;
    private JTabbedPane jettyControlTab;
    private RegisteredContextList contextList;
    private JScrollPane scrollableContextList;
        
    public JettyControl() {
        super(getMessage("main.jettycontrol.title.text"));
        init();
    }
    
    private void init() {
        getContentContainer().setLayout(new BorderLayout());
        getContentContainer().add(getCollapsibleLogPane(), BorderLayout.CENTER);
        setBorder(BorderFactory.createEmptyBorder());
        setRightDecoration(getToolbarPanel());
    }

    private JXPanel getToolbarPanel() {
        if(this.toolbarPanel != null) return this.toolbarPanel;
        toolbarPanel = new JXPanel(new FlowLayout());
        toolbarPanel.add(createButton(getAction(StringConstants.START_JETTY.getValue())));
        Action stopJetty = getAction(StringConstants.STOP_JETTY.getValue());
        stopJetty.setEnabled(false);
        toolbarPanel.add(createButton(stopJetty));
        toolbarPanel.add(getExpandCollapseButton());
        toolbarPanel.setBackground(new Color(255,255,255,0));
        return toolbarPanel;
    }
    
    private JButton createButton(Action action) {
        JButton button = new JButton(action);
        button.setSize(new Dimension(100,16));
        button.setBorder(BorderFactory.createEmptyBorder());
        button.setBackground(new Color(255,255,255,0));
        button.setHideActionText(true);
        return button;
    }
    
    private JButton getExpandCollapseButton() {
        if(this.expandCollapseButton != null) return this.expandCollapseButton;
        expandCollapseButton = new JButton(getCollapsibleLogPane().getActionMap().get(JXCollapsiblePane.TOGGLE_ACTION));
        expandCollapseButton.setBorder(BorderFactory.createEmptyBorder());
        expandCollapseButton.setSize(new Dimension(16,16));
        expandCollapseButton.setBackground(new Color(255,255,255,0));
        return expandCollapseButton;
    }
    
    private JTabbedPane getJettyControlTab() {
        if(this.jettyControlTab != null) return this.jettyControlTab;
        jettyControlTab = new JTabbedPane(JTabbedPane.BOTTOM);
        jettyControlTab.setMinimumSize(new Dimension(500, 100));
        jettyControlTab.setPreferredSize(new Dimension(500, 250));
        jettyControlTab.setMaximumSize(new Dimension(Short.MAX_VALUE, Short.MAX_VALUE));
        jettyControlTab.add(getLogViewer());
        jettyControlTab.add(getScrollableContextList());
        return jettyControlTab;
    }
    
    private JScrollPane getScrollableContextList() {
        if(this.scrollableContextList != null) return this.scrollableContextList;
        scrollableContextList = new JScrollPane(getContextList(), ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollableContextList.setMinimumSize(new Dimension(500, 100));
        scrollableContextList.setPreferredSize(new Dimension(500, 250));
        scrollableContextList.setMaximumSize(new Dimension(Short.MAX_VALUE, Short.MAX_VALUE));
        scrollableContextList.setName(getContextList().getName());
        return scrollableContextList;
    }
    
    private RegisteredContextList getContextList() {
        if(this.contextList != null) return this.contextList;
        contextList = new RegisteredContextList();
        return contextList;
    }
    
    private LogViewer getLogViewer() {
        if(this.logViewer != null) return this.logViewer;
        logViewer = new LogViewer();
        return logViewer;
    }
    
    private JXCollapsiblePane getCollapsibleLogPane() {
        if(this.collapsibleLogPane != null) return this.collapsibleLogPane;
        collapsibleLogPane = new JXCollapsiblePane(new BorderLayout());
        collapsibleLogPane.add(getJettyControlTab(), BorderLayout.CENTER);
        collapsibleLogPane.setCollapsed(true);
        collapsibleLogPane.setPreferredSize(new Dimension(500,250));
        Action toggleAction = collapsibleLogPane.getActionMap().get(JXCollapsiblePane.TOGGLE_ACTION);
        toggleAction.putValue(JXCollapsiblePane.EXPAND_ICON, new ImageIcon(getClass().getResource("/icons/expand.png")));
        toggleAction.putValue(JXCollapsiblePane.COLLAPSE_ICON, new ImageIcon(getClass().getResource("/icons/collapse.png")));
        toggleAction.putValue(Action.NAME, "");
        return collapsibleLogPane;
    }
    
    public void toggleDisplayServerLog(boolean collapse) {
        if(getCollapsibleLogPane().isCollapsed() ^ collapse) getExpandCollapseButton().doClick();
    }
    
    public void reloadContextList() {
        getContextList().reloadAllContexts();
    }
    
    public void log(String message) {
        getLogViewer().log(message);
    }
    
}
