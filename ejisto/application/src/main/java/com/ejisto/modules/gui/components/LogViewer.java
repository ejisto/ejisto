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

import static com.ejisto.util.GuiUtils.*;
import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.BorderFactory;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;

import org.jdesktop.swingx.JXPanel;

public class LogViewer extends JXPanel {
    private static final long serialVersionUID = 2849704565034218976L;
    private JTextArea log;
    private JScrollPane logPanel;
    

    public LogViewer() { 
    	super();
        init();
    }
    
    private void init() {
        setName(getMessage("main.tab.log.text"));
    	setLayout(new BorderLayout());
        add(getLogPanel(), BorderLayout.CENTER);
        setBorder(BorderFactory.createEmptyBorder());
    }
    
    private JScrollPane getLogPanel() {
        if(this.logPanel != null) return this.logPanel;
        log = new JTextArea();
        logPanel = new JScrollPane(log, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        logPanel.setMinimumSize(new Dimension(500, 100));
        logPanel.setPreferredSize(new Dimension(500, 100));
        logPanel.setMaximumSize(new Dimension(Short.MAX_VALUE, 300));
        log.setEditable(false);
        log.setFont(new java.awt.Font("Monospaced", 0, 9));
        return logPanel;
    }
    
    public void log(String message) {
        log.append(message);
    }
    
}
