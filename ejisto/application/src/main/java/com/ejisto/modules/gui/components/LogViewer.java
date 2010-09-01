package com.ejisto.modules.gui.components;

import static com.ejisto.util.GuiUtils.getMessage;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;

import org.jdesktop.swingx.JXTitledPanel;

public class LogViewer extends JXTitledPanel {
    private static final long serialVersionUID = 2849704565034218976L;
    private JTextArea log;

    public LogViewer() { 
    	super(getMessage("main.logviewer.title.text"));
        init();
    }
    
    private void init() {
    	getContentContainer().setLayout(new BorderLayout());
        log = new JTextArea();
        JScrollPane scrollPane = new JScrollPane(log, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setMinimumSize(new Dimension(500, 100));
        scrollPane.setPreferredSize(new Dimension(500, 100));
        scrollPane.setMaximumSize(new Dimension(Short.MAX_VALUE, 300));
        log.setEditable(false);
        log.setFont(new java.awt.Font("Monospaced", 0, 9));
        getContentContainer().add(scrollPane, BorderLayout.CENTER);
    }

    public void log(String message) {
        log.append(message);
    }
}
