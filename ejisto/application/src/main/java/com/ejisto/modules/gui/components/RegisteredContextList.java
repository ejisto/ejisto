/*******************************************************************************
 * Copyright 2010 Celestino Bellone
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package com.ejisto.modules.gui.components;

import static com.ejisto.util.GuiUtils.getAction;
import static com.ejisto.util.GuiUtils.getAllRegisteredContexts;
import static com.ejisto.util.GuiUtils.getMessage;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.util.Collection;

import javax.swing.Action;
import javax.swing.BorderFactory;

import org.eclipse.jetty.webapp.WebAppContext;
import org.jdesktop.swingx.JXButton;
import org.jdesktop.swingx.JXLabel;
import org.jdesktop.swingx.JXPanel;

import com.ejisto.constants.StringConstants;

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
        panel.add(getCommandButton(getAction(StringConstants.START_CONTEXT_PREFIX.getValue() + context.getContextPath()),
                getMessage("jettycontrol.context.start.text")));
        panel.add(getCommandButton(getAction(StringConstants.STOP_CONTEXT_PREFIX.getValue() + context.getContextPath()),
                getMessage("jettycontrol.context.stop.text")));
        panel.add(getCommandButton(getAction(StringConstants.DELETE_CONTEXT_PREFIX.getValue() + context.getContextPath()),
                getMessage("jettycontrol.context.delete.text")));
        panel.setMinimumSize(new Dimension(250, 30));
        panel.setMaximumSize(new Dimension(250, 30));
        panel.setPreferredSize(new Dimension(250, 30));
        panel.setBorder(BorderFactory.createLineBorder(Color.black, 1));
        return panel;
    }

    private JXLabel getLabelFor(WebAppContext context) {
        boolean active = context.isRunning();
        String color = getMessage(active ? "jettycontrol.context.active.color" : "jettycontrol.context.inactive.color");
        String status = getMessage(active ? "jettycontrol.context.active" : "jettycontrol.context.inactive");
        String message = getMessage("jettycontrol.context.template", context.getContextPath(), color, status);
        JXLabel label = new JXLabel(message);
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
