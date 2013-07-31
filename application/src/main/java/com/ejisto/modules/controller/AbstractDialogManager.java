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

import org.jdesktop.swingx.action.AbstractActionExt;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

import static com.ejisto.util.GuiUtils.getMessage;

/**
 * Created by IntelliJ IDEA.
 * User: celestino
 * Date: 6/1/12
 * Time: 8:12 AM
 */
abstract class AbstractDialogManager {
    private Action closeAction;
    private Action okAction;
    private DialogController dialogController;

    AbstractDialogManager() {
        initActions();
    }

    abstract void onAbort();

    abstract void onConfirm();

    private void initActions() {

        closeAction = new AbstractActionExt(getMessage("ok")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                ok();
            }
        };
        okAction = new AbstractActionExt(getMessage("cancel")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                close();
            }
        };
    }

    protected void openDialog(JPanel content, String title, String description, String iconKey, Dimension size) {
        dialogController = DialogController.Builder.newInstance()
                .withActions(okAction, closeAction)
                .withContent(content)
                .withHeader(title, description)
                .withIconKey(iconKey)
                .build();
        dialogController.show(true, size);
    }

    protected final void close() {
        onAbort();
        dialogController.hide();
    }

    protected final void ok() {
        onConfirm();
        dialogController.hide();
    }

}
