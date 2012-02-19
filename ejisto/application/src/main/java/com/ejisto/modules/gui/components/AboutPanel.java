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

import com.ejisto.util.IOUtils;
import org.jdesktop.swingx.JXLabel;
import org.jdesktop.swingx.JXPanel;

import javax.swing.*;
import java.awt.*;
import java.io.File;

import static com.ejisto.util.GuiUtils.getMessage;

/**
 * Created by IntelliJ IDEA.
 * User: celestino
 * Date: 1/24/12
 * Time: 8:07 PM
 */
public class AboutPanel extends JXPanel {

    private JPanel credits;
    private JScrollPane license;
    private JTabbedPane container;

    public AboutPanel() {
        super(new BorderLayout(0, 0));
        init();
    }

    private void init() {
        //add(getHeader(), BorderLayout.NORTH);
        add(getContainer(), BorderLayout.CENTER);
        setBackground(Color.WHITE);
    }

    private JTabbedPane getContainer() {
        if (container != null) return container;
        container = new JTabbedPane(JTabbedPane.BOTTOM);
        container.add(getCredits());
        container.add(getLicense());
        container.setBackground(Color.WHITE);
        container.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        return container;
    }

    private JPanel getCredits() {
        if (credits != null) return credits;
        credits = new JXPanel(new BorderLayout(5, 5));
        credits.setName(getMessage("about.tab.credits"));
        credits.add(new JXLabel(getMessage("about.credits"), JXLabel.LEFT), BorderLayout.CENTER);
        credits.setBackground(Color.WHITE);
        return credits;
    }

    private JScrollPane getLicense() {
        if (license != null) return license;
        license = new JScrollPane();
        license.setName(getMessage("about.tab.license"));
        String licenseText;
        try {
            String file = getClass().getResource("/ejisto-license.txt").getFile();
            licenseText = new String(IOUtils.readFile(new File(file)));
        } catch (Exception e) {
            licenseText = "";
        }
        JTextArea content = new JTextArea(licenseText);
        content.setEditable(false);
        license.setViewportView(content);
        license.setBackground(Color.WHITE);
        return license;
    }
}
