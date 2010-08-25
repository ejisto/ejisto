package com.ejisto.modules.gui.components;

import java.awt.Dimension;
import java.awt.FlowLayout;

import javax.swing.GroupLayout;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;

import org.jdesktop.swingx.JXPanel;

public class LogViewer extends JXPanel {
    private static final long serialVersionUID = 2849704565034218976L;
    private JTextArea log;

    public LogViewer() {
        setLayout(new FlowLayout());
        log = new JTextArea();
        log.setMinimumSize(new Dimension(200, 100));
        JScrollPane scrollPane = new JScrollPane(log, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setMinimumSize(log.getMinimumSize());
        log.setEditable(false);
        log.setFont(new java.awt.Font("Monospaced", 0, 9));
        GroupLayout logPanelLayout = new GroupLayout(this);
        setLayout(logPanelLayout);
        logPanelLayout.setHorizontalGroup(logPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addComponent(scrollPane,
                javax.swing.GroupLayout.DEFAULT_SIZE, 627, Short.MAX_VALUE));
        logPanelLayout.setVerticalGroup(logPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addComponent(scrollPane,
                javax.swing.GroupLayout.DEFAULT_SIZE, 174, Short.MAX_VALUE));
        add(scrollPane);
    }

    public void log(String message) {
        log.append(message);
    }
}
