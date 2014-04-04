/*
 * Ejisto, a powerful developer assistant
 *
 * Copyright (C) 2010-2013 Celestino Bellone
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
import com.ejisto.event.ApplicationListener;
import com.ejisto.event.def.ContainerInstalled;
import com.ejisto.event.def.SessionRecorded;
import com.ejisto.event.def.SessionRecorderStart;
import com.ejisto.event.def.StatusBarMessage;
import com.ejisto.modules.dao.entities.Container;
import com.ejisto.modules.gui.components.ContainerTab;
import com.ejisto.modules.gui.components.MainPanel;
import com.ejisto.modules.gui.components.NoContainerInstalled;
import com.ejisto.modules.recorder.CollectedData;
import com.ejisto.modules.repository.CollectedDataRepository;
import com.ejisto.modules.repository.ContainersRepository;
import com.ejisto.modules.repository.MockedFieldsRepository;
import com.ejisto.modules.repository.WebApplicationRepository;
import com.ejisto.util.GuiUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.jdesktop.swingx.JXLabel;
import org.jdesktop.swingx.JXRootPane;
import org.jdesktop.swingx.JXStatusBar;
import org.jdesktop.swingx.action.AbstractActionExt;
import org.jdesktop.swingx.plaf.basic.BasicStatusBarUI;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.List;
import java.util.Map;

import static com.ejisto.util.GuiUtils.*;
import static java.util.stream.Collectors.groupingBy;

class MainRootPane extends JXRootPane {
    private static final long serialVersionUID = -3265545519465961578L;
    private final CollectedDataRepository collectedDataRepository;
    private final ContainersRepository containersRepository;
    private final WebApplicationRepository webApplicationRepository;
    private JMenu containersMenu;
    private JMenu recordedSessionsMenu;
    private JXLabel statusLog;
    private JTabbedPane containersTabPane;
    private List<ContainerTab> containerTabs;
    private final MainPanel mainPanel;

    public MainRootPane(MockedFieldsRepository mockedFieldsRepository,
                        CollectedDataRepository collectedDataRepository,
                        ContainersRepository containersRepository,
                        WebApplicationRepository webApplicationRepository) {
        super();
        this.collectedDataRepository = collectedDataRepository;
        this.containersRepository = containersRepository;
        this.webApplicationRepository = webApplicationRepository;
        this.mainPanel = new MainPanel(mockedFieldsRepository);
        init();
        registerApplicationEventListener(new ApplicationListener<ContainerInstalled>() {
            @Override
            public void onApplicationEvent(ContainerInstalled event) {
                createContainerMenu(containersMenu, loadContainer(
                        MainRootPane.this.containersRepository,
                        event.getContainerId()));
            }

            @Override
            public Class<ContainerInstalled> getTargetEventType() {
                return ContainerInstalled.class;
            }
        });
        registerApplicationEventListener(new ApplicationListener<StatusBarMessage>() {
            @Override
            public void onApplicationEvent(StatusBarMessage event) {
                logStatusMessage(event.getMessage(), event.isError());
            }

            @Override
            public Class<StatusBarMessage> getTargetEventType() {
                return StatusBarMessage.class;
            }
        });
        registerApplicationEventListener(new ApplicationListener<ContainerInstalled>() {
            @Override
            public void onApplicationEvent(ContainerInstalled event) {
                reloadContainerTabs();
            }

            @Override
            public Class<ContainerInstalled> getTargetEventType() {
                return ContainerInstalled.class;
            }
        });
        registerApplicationEventListener(new ApplicationListener<SessionRecorded>() {
            @Override
            public void onApplicationEvent(SessionRecorded event) {
                buildRecordedSessionMenu(recordedSessionsMenu);
            }

            @Override
            public Class<SessionRecorded> getTargetEventType() {
                return SessionRecorded.class;
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
        getContentPane().add(mainPanel, BorderLayout.CENTER);
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
        containerTabs = getRegisteredContainers(containersRepository, webApplicationRepository);
        return containerTabs;
    }

    private void refreshContainerTabs(JTabbedPane mainTabbedPane) {
        containerTabs = getRegisteredContainers(containersRepository, webApplicationRepository);
        mainTabbedPane.removeAll();
        final List<ContainerTab> tabs = getContainerTabs();
        tabs.forEach(containerTab -> mainTabbedPane.addTab(containerTab.getName(), containerTab.getIcon(), containerTab));
        if (CollectionUtils.isEmpty(tabs)) {
            mainTabbedPane.addTab(getMessage("container.installation.required"),
                                  new NoContainerInstalled());
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
        recordedSessionsMenu = new JMenu("Recorded Sessions");
        JMenuItem open = new JMenuItem(getAction(StringConstants.LOAD_WEB_APP.getValue()));
        open.setText("Open");
        open.setAccelerator(
                javax.swing.KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_MASK));
        jMenuFile.add(open);

        JMenuItem jMenuItemExit = new JMenuItem(getAction(StringConstants.SHUTDOWN.getValue()));
        jMenuItemExit.setAccelerator(
                javax.swing.KeyStroke.getKeyStroke(KeyEvent.VK_D, InputEvent.CTRL_MASK));
        jMenuItemExit.setText("Exit");
        jMenuFile.add(jMenuItemExit);
        createContainerMenus(containersMenu);
        jMenuBar.add(jMenuFile);
        jMenuBar.add(containersMenu);
        //RecordedSessions
        buildRecordedSessionMenu(recordedSessionsMenu);
        jMenuBar.add(recordedSessionsMenu);
        //About menu
        JMenu jMenuHelp = new JMenu("?");
        jMenuHelp.add(getAction(StringConstants.SHOW_ABOUT_PANEL.getValue()));
        jMenuBar.add(jMenuHelp);
        setJMenuBar(jMenuBar);
    }

    private void buildRecordedSessionMenu(JMenu root) {
        if (root.getMenuComponentCount() > 0) {
            root.removeAll();
        }
        final Map<String, CollectedData> sessions = collectedDataRepository.getAllRecordedSessions();
        buildSessionMenus(root, sessions);
        if (sessions.isEmpty()) {
            JMenuItem placeHolder = new JMenuItem("<none>");
            placeHolder.setEnabled(false);
            root.add(placeHolder);
        }
        root.addSeparator();
        JMenuItem record = new JMenuItem(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                GuiUtils.publishEvent(new SessionRecorderStart(this, null));
            }
        });
        record.setText("New...");
        record.setAccelerator(
                javax.swing.KeyStroke.getKeyStroke(KeyEvent.VK_R, InputEvent.CTRL_MASK));
        root.add(record);
    }

    private void buildSessionMenus(JMenu root, Map<String, CollectedData> sessions) {
        if (sessions.isEmpty()) {
            return;
        }
        final Map<String, List<Pair<String, JCheckBoxMenuItem>>> items = sessions.entrySet().stream().map(entry -> {
            CollectedData collectedData = entry.getValue();
            JCheckBoxMenuItem item = new JCheckBoxMenuItem(new AbstractActionExt(entry.getKey()) {
                @Override
                public void actionPerformed(ActionEvent e) {
                    boolean checked = ((JCheckBoxMenuItem) e.getSource()).getState();
                    String name = e.getActionCommand();
                    if (checked) {
                        collectedDataRepository.enableRecordedSession(name);
                    } else {
                        collectedDataRepository.disableRecordedSession(name);
                    }
                }
            });
            item.setState(collectedData.isActive());
            return Pair.of(collectedData.getContextPath(), item);
        }).collect(groupingBy(Pair::getLeft));
        items.entrySet().stream().map(pair -> {
            JMenu itemMenu = new JMenu(pair.getKey());
            pair.getValue().forEach(m -> itemMenu.add(m.getValue()));
            return itemMenu;
        }).forEach(root::add);
    }


    private void createContainerMenus(JMenu root) {
        List<Container> containers = getActiveContainers(containersRepository);
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

    void logStatusMessage(String message, boolean error) {
        getStatusLog().setForeground(error ? Color.RED : Color.BLACK);
        getStatusLog().setText(message);
    }

}
