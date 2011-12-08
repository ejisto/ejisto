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

package com.ejisto.modules.controller;

import com.ejisto.modules.gui.components.EjistoDialog;

import javax.swing.*;
import java.awt.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static com.ejisto.util.GuiUtils.centerOnScreen;
import static org.springframework.util.StringUtils.hasText;

/**
 * Created by IntelliJ IDEA.
 * User: celestino
 * Date: 3/6/11
 * Time: 2:55 PM
 */
public class DialogManager {

    private final JDialog dialog;
    private final JPanel view;
    private Lock lock = new ReentrantLock();


    /**
     * Constructs a new DialogManager using a {@link JDialog} as container.
     *
     * @param parent opener Frame
     * @param view   target JPanel
     */
    private DialogManager(Frame parent, JPanel view) {
        this(new JDialog(parent), view);
    }


    /**
     * Constructs a new DialogManager using a user-defined {@link JDialog} instance as container.
     *
     * @param dialog container
     * @param view   target JPanel
     */
    private DialogManager(JDialog dialog, JPanel view) {
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
        showUndecorated(modal, new Dimension(500, 150));
    }

    /**
     * Displays an undecorated window using user-defined dimensions
     *
     * @param modal     if window should be modal in respect to container
     * @param dimension window size
     * @see #showUndecorated(boolean)
     */
    public void showUndecorated(boolean modal, Dimension dimension) {
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
        if (!lock.tryLock()) return;
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
    }

    /**
     * Hides current visible window.
     */
    public void hide() {
        dialog.setVisible(false);
        lock.unlock();
    }

//    public static final DialogManager buildNewSimpleDialog(JPanel content, Action... actions) {
//        EjistoDialog dialog = new EjistoDialog(null, content.getName(), content, true, actions);
//        return new DialogManager(dialog, content);
//    }

    public static final class Builder {

        private JPanel view;
        private String description;
        private String title;
        private boolean decorated;
        private Action[] actions;
        private Frame parent;

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

        public DialogManager build() {
            JDialog dialog;
            if (hasText(description) || hasText(title)) {
                dialog = new EjistoDialog(parent, title, view, true, actions);
                ((EjistoDialog) dialog).setHeaderTitle(title);
                ((EjistoDialog) dialog).setHeaderDescription(description);
            } else {
                dialog = new JDialog(parent);
            }
            return new DialogManager(dialog, view);
        }

    }
}
