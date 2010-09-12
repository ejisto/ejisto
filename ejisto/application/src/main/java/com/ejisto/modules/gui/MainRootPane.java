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
package com.ejisto.modules.gui;

import static com.ejisto.util.GuiUtils.getAction;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

import org.jdesktop.swingx.JXRootPane;
import org.jdesktop.swingx.JXStatusBar;

import com.ejisto.constants.StringConstants;
import com.ejisto.modules.gui.components.MainPanel;

public class MainRootPane extends JXRootPane {
    private static final long serialVersionUID = -3265545519465961578L;
    private MainPanel mainPanel;

    public MainRootPane() {
        super();
        init();
    }

    private void init() {
        initMenuBar();
        mainPanel = new MainPanel();
        getContentPane().add(mainPanel, BorderLayout.CENTER);
        setStatusBar(initStatusBar());
    }
    
    private void initMenuBar() {
        JMenuBar jMenuBar = new javax.swing.JMenuBar();
        JMenu jMenuFile = new javax.swing.JMenu("File");
        JMenu jMenuSystem = new javax.swing.JMenu("System");
        JMenuItem open = new JMenuItem(getAction(StringConstants.LOAD_WEB_APP.getValue()));
        open.setText("Open");
        open.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_O, java.awt.event.InputEvent.CTRL_MASK));
        jMenuFile.add(open);

        JMenuItem jMenuItemExit = new JMenuItem(getAction(StringConstants.SHUTDOWN.getValue()));
        jMenuItemExit.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_D, java.awt.event.InputEvent.CTRL_MASK));
        jMenuItemExit.setText("Exit");
        jMenuFile.add(jMenuItemExit);

        jMenuBar.add(jMenuFile);
        jMenuBar.add(jMenuSystem);
        setJMenuBar(jMenuBar);
    }

    public void log(String message) {
        mainPanel.log(message);
    }
    
    public void toggleDisplayServerLog(boolean collapse) {
        mainPanel.toggleDisplayServerLog(collapse);
    }
    
    public void onPropertyChange() {
    	mainPanel.onJettyStatusChange();
    }
    
    private JXStatusBar initStatusBar() {
        if (statusBar != null)
            return statusBar;
        statusBar = new JXStatusBar();
        statusBar.setMinimumSize(new Dimension(400,20));
        statusBar.setPreferredSize(new Dimension(400,20));
        statusBar.setMaximumSize(new Dimension(Short.MAX_VALUE, 20));
        statusBar.add(new JLabel("done"));
        return statusBar;
    }
    
    
    

}
