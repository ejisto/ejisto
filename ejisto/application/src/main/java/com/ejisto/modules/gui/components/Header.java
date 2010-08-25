package com.ejisto.modules.gui.components;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class Header extends JPanel implements ComponentListener {

	private static final long serialVersionUID = -8596340359045874928L;

	private JLabel logo = null;
	private JLabel gradient = null;

	/**
	 * This is the default constructor
	 */
	public Header() {
		super();
		initialize();
	}

	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize() {
		gradient = new JLabel();
		gradient.setText("");
		gradient.setIcon(getGradient(false));
		logo = new JLabel();
		logo.setText("");
		logo.setIcon(new ImageIcon(getClass().getResource(
				"/images/logo_mini.png")));
		setLayout(new BorderLayout());
		setBackground(Color.white);
		setPreferredSize(new Dimension(300, 60));
		setName("header");
		setMinimumSize(new Dimension(100, 60));
		setMaximumSize(new Dimension(32767, 60));
		setBounds(new Rectangle(0, 0, 300, 60));
		setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.gray));
		add(logo, BorderLayout.WEST);
		add(gradient, BorderLayout.EAST);
		addComponentListener(this);
	}

	private ImageIcon getGradient(boolean resized) {
		int width = 100;
		int height = 60;
		if (resized && gradient.getWidth() > 0) {
			width = gradient.getWidth();
			height = Math.max(height, gradient.getHeight());
		}
		BufferedImage bi = new BufferedImage(width, height,
				BufferedImage.TYPE_INT_RGB);
		Graphics2D g = bi.createGraphics();
		GradientPaint paint = new GradientPaint(new Point2D.Double(0, 0),
				Color.white, new Point2D.Double(width + 170, height),
				new Color(82, 139, 197));
		g.setPaint(paint);
		g.fill(new Rectangle2D.Double(0, 0, width, height));
		g.dispose();
		bi.flush();
		return new ImageIcon(bi);
	}

	@Override
	public void componentHidden(ComponentEvent e) {
	}

	@Override
	public void componentMoved(ComponentEvent e) {
	}

	@Override
	public void componentResized(ComponentEvent e) {
		gradient.setSize(getWidth() / 3, 60);
		gradient.setIcon(getGradient(true));
	}

	@Override
	public void componentShown(ComponentEvent e) {
	}

}
