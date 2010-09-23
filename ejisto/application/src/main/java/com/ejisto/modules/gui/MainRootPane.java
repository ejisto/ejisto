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

package com.ejisto.modules.gui;

import com.ejisto.constants.StringConstants;
import com.ejisto.modules.gui.components.MainPanel;
import org.jdesktop.swingx.JXLabel;
import org.jdesktop.swingx.JXRootPane;
import org.jdesktop.swingx.JXStatusBar;

import javax.swing.*;
import java.awt.*;

import static com.ejisto.util.GuiUtils.getAction;

public class MainRootPane extends JXRootPane {
    private static final long serialVersionUID = -3265545519465961578L;
    private MainPanel mainPanel;
    private JXLabel statusLabel;

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

    public void setStatusBarMessage(String messageText, boolean error) {
        statusLabel.setForeground(error ? Color.red : Color.black);
        statusLabel.setText(messageText);
    }
    
    private JXStatusBar initStatusBar() {
        if (statusBar != null)
            return statusBar;
        statusBar = new JXStatusBar();
        statusBar.setMinimumSize(new Dimension(400,20));
        statusBar.setPreferredSize(new Dimension(400,20));
        statusBar.setMaximumSize(new Dimension(Short.MAX_VALUE, 20));
        statusLabel = new JXLabel("done");
        statusBar.add(statusLabel);
        return statusBar;
    }
    
    
    

}
