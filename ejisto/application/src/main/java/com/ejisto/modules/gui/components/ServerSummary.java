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

import com.ejisto.modules.gui.EjistoAction;
import org.jdesktop.swingx.JXHeader;
import org.jdesktop.swingx.JXLabel;
import org.jdesktop.swingx.JXPanel;

import javax.swing.*;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import static com.ejisto.constants.StringConstants.START_CONTAINER;
import static com.ejisto.constants.StringConstants.STOP_CONTAINER;
import static com.ejisto.util.GuiUtils.*;

/**
 * Created by IntelliJ IDEA.
 * User: celestino
 * Date: 7/10/11
 * Time: 6:06 PM
 */
public class ServerSummary extends JXPanel implements PropertyChangeListener {

    private JXHeader header;
    private String containerId;
    private String serverName;
    private JPanel buttonsPanel;
    private JXLabel serverStatus;

    public ServerSummary(String containerId, String serverName) {
        this.containerId = containerId;
        this.serverName = serverName;
        init();
    }

    public String getContainerId() {
        return containerId;
    }

    private void init() {
        setBackground(Color.WHITE);
        setLayout(new BorderLayout());
        add(getHeader(), BorderLayout.NORTH);
        add(getButtonsPanel(), BorderLayout.CENTER);
        getAction(START_CONTAINER.getValue()).addPropertyChangeListener(this);
    }

    private JXHeader getHeader() {
        if (header != null) return header;
        header = new JXHeader(serverName, getMessage("server.summary.status"),
                              new ImageIcon(getClass().getResource("/icons/containers/tomcat.gif")));
        return header;
    }

    private JPanel getButtonsPanel() {
        if (buttonsPanel != null) return buttonsPanel;
        buttonsPanel = new JXPanel(new FlowLayout(FlowLayout.RIGHT, 5, 5));
        buttonsPanel.add(getServerStatus());
        buttonsPanel.add(createButton(getAction(START_CONTAINER.getValue())));
        Action stop = getAction(STOP_CONTAINER.getValue());
        stop.setEnabled(false);
        buttonsPanel.add(createButton(stop));
        buttonsPanel.setBackground(Color.WHITE);
        updateStatusIndicatorTooltip();
        return buttonsPanel;
    }

    private JXLabel getServerStatus() {
        if (serverStatus != null) return serverStatus;
        serverStatus = new JXLabel(getIcon(getServerStatusIcon()));
        return serverStatus;
    }

    private void updateServerStatus() {
        getServerStatus().setIcon(getIcon(getServerStatusIcon()));
        updateStatusIndicatorTooltip();
    }

    private void updateStatusIndicatorTooltip() {
        String key = getAction(
                START_CONTAINER.getValue()).isEnabled() ? "server.summary.status.indicator.tooltip.shutdown" : "server.summary.status.indicator.tooltip.running";
        getServerStatus().setToolTipText(getMessage(key));
    }

    private String getServerStatusIcon() {
        return getMessage(
                getAction(START_CONTAINER.getValue()).isEnabled() ? "server.status.shutdown" : "server.status.running");
    }

    private JButton createButton(Action action) {
        JButton button = new JButton(action);
        button.setSize(new Dimension(100, 16));
        makeTransparent(button);
        button.setRolloverIcon(getIcon(((EjistoAction<?>) action).getRolloverKey()));
        return button;
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals("enabled")) updateServerStatus();
    }
}
