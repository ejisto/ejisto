package com.ejisto.modules.gui;

import static com.ejisto.util.GuiUtils.*;

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

import org.jdesktop.swingx.JXRootPane;

import com.ejisto.event.def.LoadWebApplication;
import com.ejisto.event.def.ShutdownRequest;
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
        setContentPane(mainPanel);
    }
    
    private void initMenuBar() {
        JMenuBar jMenuBar = new javax.swing.JMenuBar();
        JMenu jMenuFile = new javax.swing.JMenu("File");
        JMenu jMenuSystem = new javax.swing.JMenu("System");
        EjistoAction<LoadWebApplication> openAction  = new EjistoAction<LoadWebApplication>(new LoadWebApplication(this));
        putAction(openAction);
        EjistoAction<ShutdownRequest> shutdownAction = new EjistoAction<ShutdownRequest>(new ShutdownRequest(this));
        putAction(shutdownAction);
        JMenuItem open = new JMenuItem(openAction);
        open.setText("Open");
        open.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_O, java.awt.event.InputEvent.CTRL_MASK));
        jMenuFile.add(open);

        JMenuItem jMenuItemExit = new javax.swing.JMenuItem();
        jMenuItemExit.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_D, java.awt.event.InputEvent.CTRL_MASK));
        jMenuItemExit.setAction(shutdownAction);
        jMenuItemExit.setText("Exit");
        jMenuFile.add(jMenuItemExit);

        jMenuBar.add(jMenuFile);
        jMenuBar.add(jMenuSystem);
        setJMenuBar(jMenuBar);
    }

    public void log(String message) {
        mainPanel.log(message);
    }
    
    public void toggleDisplayServerLog() {
        mainPanel.toggleDisplayServerLog();
    }
    
    
    

}
