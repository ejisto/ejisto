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

import javax.swing.*;
import java.awt.*;

import static com.ejisto.util.GuiUtils.centerOnScreen;

/**
 * Created by IntelliJ IDEA.
 * User: celestino
 * Date: 3/6/11
 * Time: 2:55 PM
 */
public class DialogManager {

    private final JDialog dialog;
    private final JPanel view;

    public DialogManager(Frame parent, JPanel view) {
        this.dialog = new JDialog(parent);
        this.view = view;
    }

    public void show(boolean modal) {
        dialog.setUndecorated(true);
        dialog.getContentPane().setLayout(new BorderLayout(0, 0));
        dialog.getContentPane().add(view, BorderLayout.CENTER);
        dialog.setModal(modal);
        dialog.setPreferredSize(new Dimension(500, 150));
        dialog.setSize(new Dimension(500, 150));
        centerOnScreen(dialog);
        dialog.doLayout();
        dialog.setVisible(true);
    }

    public void hide() {
        dialog.setVisible(false);
    }

}
