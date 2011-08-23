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

package com.ejisto.modules.gui.components;

import com.ejisto.event.def.ApplicationDeployed;
import com.ejisto.event.def.ContainerInstalled;
import com.ejisto.event.def.LogMessage;
import com.ejisto.modules.controller.MockedFieldsEditorController;
import org.jdesktop.swingx.JXPanel;
import org.springframework.context.ApplicationListener;

import javax.swing.*;
import java.awt.*;
import java.util.List;

import static com.ejisto.util.GuiUtils.*;

public class MainPanel extends JXPanel {
    private static final long serialVersionUID = -28148619997853619L;
    private MockedFieldsEditorController propertiesEditor;
    private Header header;
    private JTabbedPane mainTabbedPane;
    private List<ContainerTab> containerTabs;
    private final ApplicationListener<ContainerInstalled> containerInstalledListener = new ApplicationListener<ContainerInstalled>() {
        @Override
        public void onApplicationEvent(ContainerInstalled event) {
            refreshContainerTabs();
        }
    };

    private final ApplicationListener<LogMessage> logMessageListener = new ApplicationListener<LogMessage>() {
        @Override
        public void onApplicationEvent(LogMessage event) {
            log(event.getMessage());
        }
    };
    private final ApplicationListener<ApplicationDeployed> deployListener = new ApplicationListener<ApplicationDeployed>() {
        @Override
        public void onApplicationEvent(ApplicationDeployed event) {
            applicationDeployed();
        }
    };

    public MainPanel() {
        super();
        init();
    }

    public void log(String message) {
        getContainerTabs().get(0).log(message);
    }

    public void logStatusMessage(String message, boolean error) {
        if (error) getHeader().logErrorMessage(message);
        else getHeader().logInfoMessage(message);
    }

    public void applicationDeployed() {
        getContainerTabs().get(0).reloadApplications();
    }

    private void init() {
        initLayout();
        initComponents();
        runOnEDT(new Runnable() {
            @Override
            public void run() {
                registerEventListener(ContainerInstalled.class, containerInstalledListener);
                registerEventListener(LogMessage.class, logMessageListener);
                registerEventListener(ApplicationDeployed.class, deployListener);
            }
        });
    }

    private void initLayout() {
        setLayout(new BorderLayout());
    }

    private void initComponents() {
        setBackground(SystemColor.control);
        add(getHeader(), BorderLayout.NORTH);
        add(getMainTabbedPane(), BorderLayout.CENTER);
    }

    private Header getHeader() {
        if (header != null) return header;
        header = new Header(getMessage("main.header.title"), getMessage("main.header.description"));
        return header;
    }

    private JTabbedPane getMainTabbedPane() {
        if (this.mainTabbedPane != null) return this.mainTabbedPane;
        mainTabbedPane = new JTabbedPane(JTabbedPane.TOP, JTabbedPane.SCROLL_TAB_LAYOUT);
        mainTabbedPane.add(getPropertiesEditor());
        refreshContainerTabs();
        return mainTabbedPane;
    }

    private List<ContainerTab> getContainerTabs() {
        if (containerTabs != null) return containerTabs;
        containerTabs = getRegisteredContainers();
        refreshContainerTabs();
        return containerTabs;
    }

    private void refreshContainerTabs() {
        containerTabs = getRegisteredContainers();
        for (ContainerTab containerTab : getContainerTabs()) {
            mainTabbedPane.add(containerTab);
        }
    }

    private MockedFieldsEditor getPropertiesEditor() {
        if (propertiesEditor != null) return propertiesEditor.getView();
        propertiesEditor = new MockedFieldsEditorController(true);
        return propertiesEditor.getView();
    }
}
