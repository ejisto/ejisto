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

import com.ejisto.core.ApplicationException;
import com.ejisto.util.IOUtils;
import org.jdesktop.swingx.JXHyperlink;
import org.jdesktop.swingx.JXLabel;
import org.jdesktop.swingx.JXPanel;
import org.jdesktop.swingx.hyperlink.LinkModel;
import org.jdesktop.swingx.hyperlink.LinkModelAction;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import static com.ejisto.util.GuiUtils.getMessage;

/**
 * Created by IntelliJ IDEA.
 * User: celestino
 * Date: 1/24/12
 * Time: 8:07 PM
 */
public class AboutPanel extends JXPanel {

    private JScrollPane license;
    private JTabbedPane container;
    private Header header;
    private JPanel credits;

    public AboutPanel() {
        super(new BorderLayout(0, 0));
        init();
    }

    private void init() {
        add(getHeader(), BorderLayout.NORTH);
        add(getContainer(), BorderLayout.CENTER);
        setBackground(Color.WHITE);
    }

    private Header getHeader() {
        if (header != null) {
            return header;
        }
        header = new Header(getMessage("about.title"), getMessage("about.description"), "about.icon");
        return header;
    }

    private JTabbedPane getContainer() {
        if (container != null) {
            return container;
        }
        container = new JTabbedPane(JTabbedPane.BOTTOM);
        container.add(getCredits());
        container.add(getLicense());
        container.setBackground(Color.WHITE);
        container.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        return container;
    }

    private JPanel getCredits() {
        if (credits != null) {
            return credits;
        }
        credits = new JXPanel(new GridBagLayout());
        credits.add(buildSpacerElement(), buildConstraints(0, 0, true));
        credits.add(buildDescriptionLabel(getMessage("about.icon.title")), buildConstraints(0, 1, false));
        credits.add(buildDescriptionLabel(getMessage("about.icon.credits.1")), buildConstraints(0, 2, false));
        String link = getMessage("about.icon.credits.1.link");
        credits.add(new JXHyperlink(new LinkModelAction<LinkModel>(new LinkModel(link, null, createUrl(link)))),
                    buildConstraints(0, 3, false));
        credits.add(buildSpacerElement(), buildConstraints(0, 4, true));
        credits.add(buildDescriptionLabel(getMessage("about.icon.credits.2")), buildConstraints(0, 5, false));
        String link2 = getMessage("about.icon.credits.2.link");
        credits.add(new JXHyperlink(new LinkModelAction<LinkModel>(new LinkModel(link2, null, createUrl(link2)))),
                    buildConstraints(0, 6, false));
        credits.add(buildSpacerElement(), buildConstraints(0, 7, true));
        credits.add(new JSeparator(), buildConstraints(0, 8, true));
        credits.add(buildSpacerElement(), buildConstraints(0, 9, true));
        credits.add(buildDescriptionLabel(getMessage("about.credits.apache")), buildConstraints(0, 10, false));
        credits.add(buildSpacerElement(), buildConstraints(0, 11, true));
        credits.add(new JSeparator(), buildConstraints(0, 12, true));
        credits.add(buildSpacerElement(), buildConstraints(0, 13, true));
        credits.add(buildDescriptionLabel(getMessage("about.credits.thanks")), buildConstraints(0, 14, false));
        credits.add(buildSpacerElement(), buildConstraints(0, 15, true));
        credits.setMinimumSize(new Dimension(400, 300));
        credits.setBackground(Color.WHITE);
        credits.setName(getMessage("about.tab.credits"));
        return credits;
    }

    private JLabel buildDescriptionLabel(String text) {
        //JXLabel label = new JXLabel(text);
        //label.setLineWrap(true);
        return new JXLabel(text);
    }

    private JPanel buildSpacerElement() {
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        return panel;
    }

    private URL createUrl(String text) {
        try {
            return new URL(text);
        } catch (MalformedURLException e) {
            throw new ApplicationException(e);
        }
    }

    private GridBagConstraints buildConstraints(int x, int y, boolean fill) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = x;
        gbc.gridy = y;
        if (fill) {
            gbc.fill = GridBagConstraints.BOTH;
        }
        gbc.anchor = GridBagConstraints.BASELINE_LEADING;
        return gbc;
    }


    private JScrollPane getLicense() {
        if (license != null) {
            return license;
        }
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
