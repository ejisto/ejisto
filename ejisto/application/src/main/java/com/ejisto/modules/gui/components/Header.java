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

import org.jdesktop.swingx.JXHeader;
import org.jdesktop.swingx.JXPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

public class Header extends JXPanel implements ComponentListener {

    private static final long serialVersionUID = -8596340359045874928L;

    private JLabel gradient = null;

    private JXHeader header;

    private String title;

    private String description;

    public Header() {
        this(null, null);
    }

    public Header(String title) {
        this(title, null);
    }

    public Header(String title, String description) {
        super();
        this.title = title;
        this.description = description;
        initialize();
    }

    public void setTitle(String title) {
        this.title = title;
        getHeader().setTitle(title);
    }

    public void logInfoMessage(String message) {
        getHeader().setDescriptionForeground(Color.black);
        getHeader().setDescription(message);
    }

    public void logErrorMessage(String message) {
        getHeader().setDescriptionForeground(Color.red);
        getHeader().setDescription(message);
    }

    public void setDescription(String description) {
        this.description = description;
        getHeader().setDescription(description);
    }

    /**
     * This method initializes this
     *
     * @return void
     */
    private void initialize() {
        setBackground(Color.white);
        gradient = new JLabel();
        gradient.setText("");
        gradient.setIcon(getGradient(false));
        JLabel logo = new JLabel();
        logo.setText("");
        logo.setIcon(new ImageIcon(getClass().getResource("/images/logo_mini.png")));
        setLayout(new BorderLayout());
        setBackground(Color.white);
        setPreferredSize(new Dimension(500, 80));
        setName("header");
        setMinimumSize(new Dimension(300, 80));
        setMaximumSize(new Dimension(32767, 100));
        setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.gray));
        add(logo, BorderLayout.WEST);
//        add(gradient, BorderLayout.EAST);
        add(getHeader(), BorderLayout.CENTER);
        addComponentListener(this);
    }

    private JXHeader getHeader() {
        if (this.header != null) return this.header;
        header = new JXHeader();
        header.setPreferredSize(new Dimension(350, 60));
        header.setMaximumSize(new Dimension(32767, 60));
        if (title != null) header.setTitle(title);
        if (description != null) header.setDescription(description);
        header.setBackground(Color.white);
        return header;
    }

    private ImageIcon getGradient(boolean resized) {
        int width = 100;
        int height = 60;
        if (resized && gradient.getWidth() > 0) {
            width = gradient.getWidth();
            height = Math.max(height, gradient.getHeight());
        }
        BufferedImage bi = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = bi.createGraphics();
        GradientPaint paint = new GradientPaint(new Point2D.Double(0, 0), Color.white, new Point2D.Double(width + 170, height),
                                                new Color(82, 139, 197));
        g.setPaint(paint);
        g.fill(new Rectangle2D.Double(0, 0, width, height));
        g.dispose();
        bi.flush();
        return new ImageIcon(bi);
    }

    @Override
    public void componentHidden(ComponentEvent e) {}

    @Override
    public void componentMoved(ComponentEvent e) {}

    @Override
    public void componentResized(ComponentEvent e) {
        gradient.setSize(getWidth() / 3, 60);
        gradient.setIcon(getGradient(true));
    }

    @Override
    public void componentShown(ComponentEvent e) {}
}
