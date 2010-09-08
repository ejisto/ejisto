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

import static com.ejisto.util.GuiUtils.getAction;
import static com.ejisto.util.GuiUtils.getMessage;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;

import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;

import org.jdesktop.swingx.JXCollapsiblePane;
import org.jdesktop.swingx.JXPanel;
import org.jdesktop.swingx.JXTitledPanel;

import com.ejisto.constants.StringConstants;

public class LogViewer extends JXTitledPanel {
    private static final long serialVersionUID = 2849704565034218976L;
    private JTextArea log;
    private JXCollapsiblePane collapsibleLogPane; 
    private JButton expandCollapseButton;
    private JXPanel toolbarPanel;

    public LogViewer() { 
    	super(getMessage("main.logviewer.title.text"));
        init();
    }
    
    private void init() {
    	getContentContainer().setLayout(new BorderLayout());
        getContentContainer().add(getCollapsibleLogPane(), BorderLayout.CENTER);
        setRightDecoration(getToolbarPanel());
    }
    
    private JXCollapsiblePane getCollapsibleLogPane() {
        if(this.collapsibleLogPane != null) return this.collapsibleLogPane;
        collapsibleLogPane = new JXCollapsiblePane();
        log = new JTextArea();
        JScrollPane scrollPane = new JScrollPane(log, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setMinimumSize(new Dimension(500, 100));
        scrollPane.setPreferredSize(new Dimension(500, 100));
        scrollPane.setMaximumSize(new Dimension(Short.MAX_VALUE, 300));
        log.setEditable(false);
        log.setFont(new java.awt.Font("Monospaced", 0, 9));
        collapsibleLogPane.add(scrollPane);
        collapsibleLogPane.setCollapsed(true);
        Action toggleAction = collapsibleLogPane.getActionMap().get(JXCollapsiblePane.TOGGLE_ACTION);
        toggleAction.putValue(JXCollapsiblePane.EXPAND_ICON, new ImageIcon(getClass().getResource("/icons/expand.png")));
        toggleAction.putValue(JXCollapsiblePane.COLLAPSE_ICON, new ImageIcon(getClass().getResource("/icons/collapse.png")));
        toggleAction.putValue(Action.NAME, "");
        return collapsibleLogPane;
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

    public void log(String message) {
        log.append(message);
    }
    
    public void toggleDisplayServerLog(boolean collapse) {
        if(getCollapsibleLogPane().isCollapsed() ^ collapse) getExpandCollapseButton().doClick();
    }
}
