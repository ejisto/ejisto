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

package com.ejisto.modules.controller;

import com.ejisto.modules.gui.components.EjistoDialog;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyListener;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static com.ejisto.modules.gui.components.EjistoDialog.DEFAULT_WIDTH;
import static com.ejisto.util.GuiUtils.centerOnScreen;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

/**
 * Created by IntelliJ IDEA.
 * User: celestino
 * Date: 3/6/11
 * Time: 2:55 PM
 */
public final class DialogController {

    private final JDialog dialog;
    private final JPanel view;
    private Lock lock = new ReentrantLock();


    /**
     * Constructs a new DialogController using a user-defined {@link JDialog} instance as container.
     *
     * @param dialog container
     * @param view   target JPanel
     */
    private DialogController(JDialog dialog, JPanel view) {
        this.dialog = dialog;
        this.view = view;
    }

    /**
     * Displays an undecorated window with default size of 500x150 px
     *
     * @param modal if window should be modal in respect to container
     * @see #showUndecorated(boolean, java.awt.Dimension)
     */
    public void showUndecorated(boolean modal) {
        showUndecorated(modal, new Dimension(DEFAULT_WIDTH, 150));
    }

    /**
     * Displays an undecorated window using user-defined dimensions
     *
     * @param modal     if window should be modal in respect to container
     * @param dimension window size
     * @see #showUndecorated(boolean)
     */
    void showUndecorated(boolean modal, Dimension dimension) {
        dialog.setUndecorated(true);
        show(modal, dimension);
    }

    /**
     * Displays a window using user-defined dimensions
     *
     * @param modal     if window should be modal in respect to container
     * @param dimension window size
     */
    public void show(boolean modal, Dimension dimension) {
        if (!lock.tryLock()) {
            return;
        }
        if (view != null) {
            dialog.getContentPane().setLayout(new BorderLayout(0, 0));
            dialog.getContentPane().add(view, BorderLayout.CENTER);
        }
        dialog.setModal(modal);
        dialog.setPreferredSize(dimension);
        dialog.setSize(dimension);
        centerOnScreen(dialog);
        dialog.doLayout();
        dialog.setVisible(true);
        lock.unlock();
    }

    /**
     * Hides current visible window.
     */
    public void hide() {
        dialog.setVisible(false);
    }

    public static final class Builder {

        private JPanel view;
        private String description;
        private String title;
        private boolean decorated = true;
        private Action[] actions;
        private Frame parent;
        private KeyListener keyListener;
        private String iconKey;
        private boolean resizable = true;

        public static Builder newInstance() {
            return new Builder();
        }


        private Builder() {
        }

        public Builder withHeader(String title, String description) {
            this.title = title;
            this.description = description;
            return this;
        }

        public Builder withContent(JPanel content) {
            this.view = content;
            return this;
        }

        public Builder withDecorations(boolean decorations) {
            this.decorated = decorations;
            return this;
        }

        public Builder withActions(Action... actions) {
            this.actions = actions;
            return this;
        }

        public Builder withParentFrame(Frame parent) {
            this.parent = parent;
            return this;
        }

        public Builder withIconKey(String iconKey) {
            this.iconKey = iconKey;
            return this;
        }

        public Builder withKeyListener(KeyListener keyListener) {
            this.keyListener = keyListener;
            return this;
        }

        public Builder resizable(boolean resizable) {
            this.resizable = resizable;
            return this;
        }

        public DialogController build() {
            JDialog dialog;
            if (isNotBlank(description) || isNotBlank(title)) {
                dialog = new EjistoDialog(parent, title, view, true, iconKey, actions);
                ((EjistoDialog) dialog).setHeaderTitle(title);
                ((EjistoDialog) dialog).setHeaderDescription(description);
            } else {
                dialog = new JDialog(parent);
            }
            dialog.setUndecorated(!decorated);
            dialog.setResizable(resizable);
            if (keyListener != null) {
                dialog.addKeyListener(keyListener);
            }
            return new DialogController(dialog, view);
        }

    }
}
