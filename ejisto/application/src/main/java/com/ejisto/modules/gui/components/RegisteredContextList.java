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

import com.ejisto.constants.StringConstants;
import com.ejisto.util.GuiUtils;
import org.eclipse.jetty.webapp.WebAppContext;
import org.jdesktop.swingx.JXButton;
import org.jdesktop.swingx.JXLabel;
import org.jdesktop.swingx.JXPanel;

import javax.swing.*;
import java.awt.*;
import java.util.Collection;

import static com.ejisto.util.GuiUtils.*;

public class RegisteredContextList extends JXPanel {
    private static final long serialVersionUID = 7817710546838911106L;

    public RegisteredContextList() {
        super();
        init();
    }

    public void reloadAllContexts() {
        internalReloadAllContexts(true);
    }

    private void internalReloadAllContexts(boolean removeAll) {
        if (removeAll) removeAll();
        Collection<WebAppContext> contexts = getAllRegisteredContexts();
        for (WebAppContext context : contexts) {
            add(buildContextControlPanel(context));
        }
        revalidate();
        repaint();
    }

    private void init() {
        setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        setName(getMessage("main.tab.webappcontext.text"));
        setMinimumSize(new Dimension(500, 200));
//        setPreferredSize(new Dimension(500, 200));
//        setMaximumSize(new Dimension(Short.MAX_VALUE, 300));
        setLayout(new FlowLayout(FlowLayout.LEADING));
        internalReloadAllContexts(false);
    }

    private JXPanel buildContextControlPanel(WebAppContext context) {
        JXPanel panel = new JXPanel(new FlowLayout(FlowLayout.LEADING));
        panel.add(getLabelFor(context));
        panel.add(
                getCommandButton(getAction(StringConstants.START_CONTEXT_PREFIX.getValue() + context.getContextPath()),
                                 getMessage("jettycontrol.context.start.text")));
        panel.add(getCommandButton(getAction(StringConstants.STOP_CONTEXT_PREFIX.getValue() + context.getContextPath()),
                                   getMessage("jettycontrol.context.stop.text")));
        panel.add(
                getCommandButton(getAction(StringConstants.DELETE_CONTEXT_PREFIX.getValue() + context.getContextPath()),
                                 getMessage("jettycontrol.context.delete.text")));
        panel.setMinimumSize(new Dimension(210, 30));
        panel.setMaximumSize(new Dimension(210, 30));
        panel.setPreferredSize(new Dimension(210, 30));
        panel.setBorder(BorderFactory.createLineBorder(Color.black, 1));
        return panel;
    }

    private JXLabel getLabelFor(WebAppContext context) {
        boolean active = context.isRunning();
        String color = getMessage(active ? "jettycontrol.context.active.color" : "jettycontrol.context.inactive.color");
        String status = getMessage(active ? "jettycontrol.context.active" : "jettycontrol.context.inactive");
        String message = getMessage("jettycontrol.context.template", context.getContextPath(), color, status);
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
        return button;
    }
}
