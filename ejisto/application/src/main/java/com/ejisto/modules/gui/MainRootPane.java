/*
 * Ejisto, a powerful developer assistant
 *
 * Copyright (C) 2010-2011  Celestino Bellone
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
import com.ejisto.modules.controller.TaskController;
import com.ejisto.modules.dao.entities.Container;
import com.ejisto.modules.gui.components.MainPanel;
import com.ejisto.modules.gui.components.TaskView;
import com.ejisto.util.GuiUtils;
import org.jdesktop.swingx.JXRootPane;
import org.springframework.context.ApplicationListener;

import javax.swing.*;
import java.awt.*;
import java.util.List;

import static com.ejisto.util.GuiUtils.*;

public class MainRootPane extends JXRootPane {
    private static final long serialVersionUID = -3265545519465961578L;
    private TaskController taskController;
    private JMenu containersMenu;

    public MainRootPane() {
        super();
        init();
        registerEventListener(ContainerInstalled.class, new ApplicationListener<ContainerInstalled>() {
            @Override
            public void onApplicationEvent(ContainerInstalled event) {
                createContainerMenu(containersMenu, GuiUtils.loadContainer(event.getContainerId()));
            }
        });
    }

    private void init() {
        initMenuBar();
        getContentPane().add(new MainPanel(), BorderLayout.CENTER);
    }

    private void initMenuBar() {
        JMenuBar jMenuBar = new javax.swing.JMenuBar();
        JMenu jMenuFile = new javax.swing.JMenu("File");
        containersMenu = new javax.swing.JMenu("Containers");
        JMenuItem open = new JMenuItem(getAction(StringConstants.LOAD_WEB_APP.getValue()));
        open.setText("Open");
        open.setAccelerator(
                javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_O, java.awt.event.InputEvent.CTRL_MASK));
        jMenuFile.add(open);

        JMenuItem jMenuItemExit = new JMenuItem(getAction(StringConstants.SHUTDOWN.getValue()));
        jMenuItemExit.setAccelerator(
                javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_D, java.awt.event.InputEvent.CTRL_MASK));
        jMenuItemExit.setText("Exit");
        jMenuFile.add(jMenuItemExit);
        createContainerMenus(containersMenu);
        jMenuBar.add(jMenuFile);
        jMenuBar.add(containersMenu);
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

    private TaskView initTaskView() {
        if (this.taskController == null) this.taskController = new TaskController();
        return taskController.getView();
    }

}
