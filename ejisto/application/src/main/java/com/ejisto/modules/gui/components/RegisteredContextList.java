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

package com.ejisto.modules.gui.components;

import com.ejisto.core.container.WebApplication;
import com.ejisto.event.def.ApplicationDeployed;
import com.ejisto.event.def.ChangeWebAppContextStatus;
import com.ejisto.event.def.ContainerStatusChanged;
import com.ejisto.util.GuiUtils;
import org.jdesktop.swingx.JXButton;
import org.jdesktop.swingx.JXLabel;
import org.jdesktop.swingx.JXPanel;
import org.springframework.context.ApplicationListener;

import javax.swing.*;
import java.awt.*;
import java.util.List;

import static ch.lambdaj.Lambda.flatten;
import static com.ejisto.constants.StringConstants.*;
import static com.ejisto.util.GuiUtils.*;

public class RegisteredContextList extends JXPanel {

    private static final long serialVersionUID = -157871898009911909L;
    private transient final ApplicationListener<ChangeWebAppContextStatus> contextChangeListener = new ApplicationListener<ChangeWebAppContextStatus>() {
        @Override
        public void onApplicationEvent(ChangeWebAppContextStatus event) {
            reloadAllContexts();
        }
    };

    private transient final ApplicationListener<ApplicationDeployed> applicationDeployListener = new ApplicationListener<ApplicationDeployed>() {
        @Override
        public void onApplicationEvent(ApplicationDeployed event) {
            reloadAllContexts();
        }
    };

    private transient final ApplicationListener<ContainerStatusChanged> serverStartListener = new ApplicationListener<ContainerStatusChanged>() {
        @Override
        public void onApplicationEvent(ContainerStatusChanged event) {
            boolean started = event.isStarted();
            for (WebApplication<?> application : getAllRegisteredWebApplications()) {
                getAction(buildCommand(START_CONTEXT_PREFIX, application.getContainerId(),
                                       application.getWebApplicationContextPath())).setEnabled(!started);
                getAction(buildCommand(STOP_CONTEXT_PREFIX, application.getContainerId(),
                                       application.getWebApplicationContextPath())).setEnabled(started);
            }
            reloadAllContexts();
        }
    };


    public RegisteredContextList() {
        super();
        init();
    }

    public void reloadAllContexts() {
        internalReloadAllContexts(true);
    }

    private List<WebApplication<?>> getAllRegisteredWebApplications() {
        return flatten(getAllRegisteredContexts());
    }

    private void internalReloadAllContexts(boolean removeAll) {
        if (removeAll) {
            removeAll();
        }
        for (WebApplication<?> context : getAllRegisteredWebApplications()) {
            add(buildContextControlPanel(context));
        }
        revalidate();
        repaint();
    }

    private void init() {
        registerEventListener(ChangeWebAppContextStatus.class, contextChangeListener);
        registerEventListener(ApplicationDeployed.class, applicationDeployListener);
        registerEventListener(ContainerStatusChanged.class, serverStartListener);
        setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        setBackground(Color.WHITE);
        setName(getMessage("main.tab.webappcontext.text"));
        setMinimumSize(new Dimension(250, 200));
        setLayout(new FlowLayout(FlowLayout.LEADING));
        internalReloadAllContexts(false);
    }

    private JXPanel buildContextControlPanel(WebApplication<?> context) {
        String contextPath = context.getWebApplicationContextPath();
        String containerId = context.getContainerId();
        JXPanel panel = new JXPanel(new FlowLayout(FlowLayout.LEADING));
        panel.setBackground(Color.WHITE);
        panel.add(getLabelFor(context));
        panel.add(getCommandButton(getAction(buildCommand(START_CONTEXT_PREFIX, containerId, contextPath)),
                                   getMessage("webapp.context.start.text")));
        panel.add(getCommandButton(getAction(buildCommand(STOP_CONTEXT_PREFIX, containerId, contextPath)),
                                   getMessage("webapp.context.stop.text")));
        panel.add(
                getCommandButton(getAction(buildCommand(DELETE_CONTEXT_PREFIX, containerId, contextPath)),
                                 getMessage("webapp.context.delete.text")));
        panel.setMinimumSize(new Dimension(210, 30));
        panel.setMaximumSize(new Dimension(210, 30));
        panel.setPreferredSize(new Dimension(210, 30));
        panel.setBorder(BorderFactory.createLineBorder(Color.black, 1));
        return panel;
    }

    private JXLabel getLabelFor(WebApplication<?> context) {
        boolean active = context.isRunning();
        String color = getMessage(active ? "webapp.context.active.color" : "webapp.context.inactive.color");
        String status = getMessage(active ? "webapp.context.active" : "webapp.context.inactive");
        String message = getMessage("webapp.context.template", context.getWebApplicationContextPath(), color, status);
        JXLabel label = new JXLabel(message);
        label.setFont(GuiUtils.getDefaultFont());
        return label;
    }

    private JXButton getCommandButton(Action action, String toolTipText) {
        JXButton button = new JXButton(action);
        button.putClientProperty("hideActionText", Boolean.TRUE);
        button.setBorder(BorderFactory.createEmptyBorder());
        button.setBackground(new Color(0f, 0f, 0f, 0f));
        button.setToolTipText(toolTipText);
        disableFocusPainting(button);
        return button;
    }
}
