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

package com.ejisto.modules.gui.components;

import com.ejisto.event.ApplicationListener;
import com.ejisto.event.def.ApplicationDeployed;
import com.ejisto.event.def.LogMessage;
import com.ejisto.modules.repository.WebApplicationRepository;
import org.jdesktop.swingx.JXPanel;

import javax.swing.*;
import java.awt.*;

import static com.ejisto.constants.StringConstants.DEFAULT_CARGO_ID;
import static com.ejisto.modules.gui.components.EjistoDialog.DEFAULT_WIDTH;
import static com.ejisto.util.GuiUtils.getMessage;
import static com.ejisto.util.GuiUtils.registerApplicationEventListener;
import static java.lang.String.format;

/**
 * Created by IntelliJ IDEA.
 * User: celestino
 * Date: 6/2/11
 * Time: 5:26 PM
 */
public class ContainerTab extends JSplitPane {

    static final int MINIMUM_HEIGHT = 150;
    private String containerId;
    private LogViewer logViewer;
    private JScrollPane scrollableContextList;
    private RegisteredContextList contextList;
    private JPanel serverSummaryPane;
    private String serverName;
    private final WebApplicationRepository webApplicationRepository;

    public ContainerTab(String serverName,
                        String containerId,
                        WebApplicationRepository webApplicationRepository) {
        super();
        this.containerId = containerId;
        this.serverName = serverName;
        this.webApplicationRepository = webApplicationRepository;
        setName(serverName);
        init();
    }

    private void log(LogMessage event) {
        if (containerId.equals(event.getContainerId())) {
            getLogViewer().log(event.getMessage());
        }
    }

    public String getContainerId() {
        return containerId;
    }

    public void reloadApplications() {
        getContextList().reloadAllContexts();
    }

    public Icon getIcon() {
        //loading default icon
        return new ImageIcon(
                getClass().getResource(getMessage(format("container.%s.icon", DEFAULT_CARGO_ID.getValue()))));
    }

    private void init() {
        setOrientation(HORIZONTAL_SPLIT);
        initLeftComponent();
        initRightComponent();
        setDividerSize(2);
        setResizeWeight(1.0D);
        registerApplicationEventListener(
                new ApplicationListener<ApplicationDeployed>() {
                    @Override
                    public void onApplicationEvent(ApplicationDeployed event) {
                        applicationDeployed(event);
                    }

                    @Override
                    public Class<ApplicationDeployed> getTargetEventType() {
                        return ApplicationDeployed.class;
                    }
                });
        registerApplicationEventListener(
                new ApplicationListener<LogMessage>() {
                    @Override
                    public void onApplicationEvent(final LogMessage event) {
                        if (containerId.equals(
                                event.getContainerId())) {
                            log(event);
                        }
                    }

                    @Override
                    public Class<LogMessage> getTargetEventType() {
                        return LogMessage.class;
                    }
                });
    }

    private void applicationDeployed(ApplicationDeployed event) {
        if (containerId.equals(event.getContainerId())) {
            reloadApplications();
        }
    }

    private void initLeftComponent() {
        JXPanel leftComponent = new JXPanel(new BorderLayout());
        leftComponent.add(getServerSummaryPane(), BorderLayout.NORTH);
        leftComponent.add(getLogViewer(), BorderLayout.CENTER);
        leftComponent.setPreferredSize(new Dimension(DEFAULT_WIDTH, MINIMUM_HEIGHT));
        leftComponent.setMinimumSize(new Dimension(DEFAULT_WIDTH, MINIMUM_HEIGHT));
        leftComponent.setMaximumSize(new Dimension(Short.MAX_VALUE, Short.MAX_VALUE));
        setLeftComponent(leftComponent);
    }

    private void initRightComponent() {
        setRightComponent(getScrollableContextList());
    }

    private JPanel getServerSummaryPane() {
        if (this.serverSummaryPane != null) {
            return serverSummaryPane;
        }
        serverSummaryPane = new ServerSummary(containerId, serverName);
        return serverSummaryPane;
    }

    private LogViewer getLogViewer() {
        if (this.logViewer != null) {
            return this.logViewer;
        }
        logViewer = new LogViewer();
        return logViewer;
    }

    private JScrollPane getScrollableContextList() {
        if (this.scrollableContextList != null) {
            return this.scrollableContextList;
        }
        scrollableContextList = new JScrollPane(getContextList(), ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
                                                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollableContextList.setMinimumSize(new Dimension(250, 100));
        scrollableContextList.setPreferredSize(new Dimension(250, MINIMUM_HEIGHT));
        scrollableContextList.setMaximumSize(new Dimension(250, Short.MAX_VALUE));
        scrollableContextList.setName(getContextList().getName());

        return scrollableContextList;
    }

    private RegisteredContextList getContextList() {
        if (this.contextList != null) {
            return this.contextList;
        }
        contextList = new RegisteredContextList(webApplicationRepository);
        return contextList;
    }

}
