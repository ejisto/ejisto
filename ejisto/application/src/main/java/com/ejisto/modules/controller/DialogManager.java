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

import org.jdesktop.swingx.JXDialog;

import javax.swing.*;
import java.awt.*;

/**
 * Created by IntelliJ IDEA.
 * User: celestino
 * Date: 3/6/11
 * Time: 2:55 PM
 */
public class DialogManager {

    private JXDialog dialog;

    public DialogManager(Frame parent, JPanel view) {
        this.dialog = new JXDialog(parent, view);
    }

    public void show(boolean modal) {
        dialog.setModal(modal);
        dialog.setVisible(true);
    }

    public void showInSeparateThread(boolean modal) {
        dialog.setModal(modal);
        new Thread() {
            @Override
            public void run() {
                dialog.setVisible(true);
            }
        }.start();
    }

    public void hide() {
        dialog.setVisible(false);
    }

}
