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

import org.jdesktop.swingx.JXPanel;

import javax.swing.*;
import java.awt.*;

import static com.ejisto.util.GuiUtils.getMessage;

public class LogViewer extends JXPanel {
    private static final long serialVersionUID = 2849704565034218976L;
    private JTextArea log;
    private JScrollPane logPanel;

    public LogViewer() {
        super();
        init();
    }

    private void init() {
        setName(getMessage("main.tab.log.text"));
        setLayout(new BorderLayout());
        add(getLogPanel(), BorderLayout.CENTER);
        setBorder(BorderFactory.createEmptyBorder());
    }

    private JScrollPane getLogPanel() {
        if (this.logPanel != null) return this.logPanel;
        log = new BufferedTextArea(1000);
        logPanel = new JScrollPane(log, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        logPanel.setMinimumSize(new Dimension(500, 100));
//        logPanel.setPreferredSize(new Dimension(500, 100));
//        logPanel.setMaximumSize(new Dimension(Short.MAX_VALUE, 300));
        log.setEditable(false);
        log.setFont(new java.awt.Font("Monospaced", 0, 9));
        return logPanel;
    }

    public void log(String message) {
        log.append(message);
    }

}
