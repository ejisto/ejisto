/*
 * Ejisto, a powerful developer assistant
 *
 * Copyright (C) 2010-2012  Celestino Bellone
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
import com.ejisto.event.def.ContainerInstalled;
import com.ejisto.event.def.SessionRecorderStart;
import com.ejisto.event.def.StatusBarMessage;
import com.ejisto.modules.dao.entities.Container;
import com.ejisto.modules.gui.components.ContainerTab;
import com.ejisto.modules.gui.components.MainPanel;
import com.ejisto.util.GuiUtils;
import org.jdesktop.swingx.JXLabel;
import org.jdesktop.swingx.JXRootPane;
import org.jdesktop.swingx.JXStatusBar;
import org.jdesktop.swingx.plaf.basic.BasicStatusBarUI;
import org.springframework.context.ApplicationListener;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.List;

import static com.ejisto.util.GuiUtils.*;

public class MainRootPane extends JXRootPane {
    private static final long serialVersionUID = -3265545519465961578L;
    //    private TaskController taskController;
    private JMenu containersMenu;
    private JXLabel statusLog;
    private JTabbedPane containersTabPane;
    private List<ContainerTab> containerTabs;

    public MainRootPane() {
        super();
        init();
        registerEventListener(ContainerInstalled.class, new ApplicationListener<ContainerInstalled>() {
            @Override
            public void onApplicationEvent(ContainerInstalled event) {
                createContainerMenu(containersMenu, GuiUtils.loadContainer(event.getContainerId()));
            }
        });
        registerEventListener(StatusBarMessage.class, new ApplicationListener<StatusBarMessage>() {
            @Override
            public void onApplicationEvent(StatusBarMessage event) {
                logStatusMessage(event.getMessage(), event.isError());
            }
        });
        registerEventListener(ContainerInstalled.class, new ApplicationListener<ContainerInstalled>() {
            @Override
            public void onApplicationEvent(ContainerInstalled event) {
                reloadContainerTabs();
            }
        });


    }

    private void reloadContainerTabs() {
        if (containersTabPane == null) {
            getContainersTabPane();
        } else {
            refreshContainerTabs(getContainersTabPane());
        }
    }

    private void init() {
        initMenuBar();
        getContentPane().add(new MainPanel(), BorderLayout.CENTER);
        getContentPane().add(getContainersTabPane(), BorderLayout.SOUTH);
        initStatusBar();
    }

    private JTabbedPane getContainersTabPane() {
        if (containersTabPane != null) {
            return containersTabPane;
        }
        containersTabPane = new JTabbedPane();
        refreshContainerTabs(containersTabPane);
        return containersTabPane;
    }

    private List<ContainerTab> getContainerTabs() {
        if (containerTabs != null) {
            return containerTabs;
        }
        containerTabs = getRegisteredContainers();
        return containerTabs;
    }

    private void refreshContainerTabs(JTabbedPane mainTabbedPane) {
        containerTabs = getRegisteredContainers();
        for (ContainerTab containerTab : getContainerTabs()) {
            mainTabbedPane.addTab(containerTab.getName(), containerTab.getIcon(), containerTab);
        }
    }

    private void initStatusBar() {
        JXStatusBar statusBar = new JXStatusBar();
        statusBar.putClientProperty(BasicStatusBarUI.AUTO_ADD_SEPARATOR, true);
        statusBar.add(getStatusLog());
        setStatusBar(statusBar);
    }

    private JXLabel getStatusLog() {
        if (statusLog != null) {
            return statusLog;
        }
        statusLog = new JXLabel(getMessage("main.header.description"));
        return statusLog;
    }

    private void initMenuBar() {
        JMenuBar jMenuBar = new JMenuBar();
        JMenu jMenuFile = new JMenu("File");
        containersMenu = new JMenu("Containers");
        JMenuItem open = new JMenuItem(getAction(StringConstants.LOAD_WEB_APP.getValue()));
        open.setText("Open");
        open.setAccelerator(
                javax.swing.KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_MASK));
        jMenuFile.add(open);

        JMenuItem record = new JMenuItem(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                GuiUtils.publishEvent(new SessionRecorderStart(this, "/petclinic"));
            }
        });
        record.setText("-TEST- Record");
        record.setAccelerator(
                javax.swing.KeyStroke.getKeyStroke(KeyEvent.VK_R, InputEvent.CTRL_MASK));
        jMenuFile.add(record);

        JMenuItem jMenuItemExit = new JMenuItem(getAction(StringConstants.SHUTDOWN.getValue()));
        jMenuItemExit.setAccelerator(
                javax.swing.KeyStroke.getKeyStroke(KeyEvent.VK_D, InputEvent.CTRL_MASK));
        jMenuItemExit.setText("Exit");
        jMenuFile.add(jMenuItemExit);
        createContainerMenus(containersMenu);
        jMenuBar.add(jMenuFile);
        jMenuBar.add(containersMenu);
        //About menu
        JMenu jMenuHelp = new JMenu("?");
        jMenuHelp.add(getAction(StringConstants.SHOW_ABOUT_PANEL.getValue()));
        jMenuBar.add(jMenuHelp);
        setJMenuBar(jMenuBar);
    }

    private void createContainerMenus(JMenu root) {
        List<Container> containers = getActiveContainers();
        for (Container container : containers) {
            createContainerMenu(root, container);
        }
    }

    private void createContainerMenu(JMenu root, Container container) {
        JMenu menu = new JMenu(container.getDescription());
        menu.add(getAction(StringConstants.START_CONTAINER.getValue()));
        menu.add(getAction(StringConstants.STOP_CONTAINER.getValue()));
        root.add(menu);
    }

//    private TaskView initTaskView() {
//        if (this.taskController == null) this.taskController = new TaskController();
//        return taskController.getView();
//    }

    void logStatusMessage(String message, boolean error) {
        getStatusLog().setForeground(error ? Color.RED : Color.BLACK);
        getStatusLog().setText(message);
    }

}
