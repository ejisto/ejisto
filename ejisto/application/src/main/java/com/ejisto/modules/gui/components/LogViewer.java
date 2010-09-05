package com.ejisto.modules.gui.components;

import static com.ejisto.util.GuiUtils.getMessage;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;

import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;

import org.jdesktop.swingx.JXCollapsiblePane;
import org.jdesktop.swingx.JXTitledPanel;

public class LogViewer extends JXTitledPanel {
    private static final long serialVersionUID = 2849704565034218976L;
    private JTextArea log;
    private JXCollapsiblePane collapsibleLogPane; 
    private JButton expandCollapseButton;

    public LogViewer() { 
    	super(getMessage("main.logviewer.title.text"));
        init();
    }
    
    private void init() {
    	getContentContainer().setLayout(new BorderLayout());
        getContentContainer().add(getCollapsibleLogPane(), BorderLayout.CENTER);
        setRightDecoration(getExpandCollapseButton());
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
    
    private JButton getExpandCollapseButton() {
        if(this.expandCollapseButton != null) return this.expandCollapseButton;
        expandCollapseButton = new JButton(getCollapsibleLogPane().getActionMap().get(JXCollapsiblePane.TOGGLE_ACTION));
        expandCollapseButton.setBorder(BorderFactory.createEmptyBorder());
        expandCollapseButton.setBackground(new Color(255,255,255,0));
        return expandCollapseButton;
    }

    public void log(String message) {
        log.append(message);
    }
    
    public void toggleDisplayServerLog() {
        getExpandCollapseButton().doClick();
    }
}
