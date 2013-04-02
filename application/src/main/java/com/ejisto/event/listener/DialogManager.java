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

package com.ejisto.event.listener;

import com.ejisto.event.def.DialogRequested;
import com.ejisto.modules.controller.DialogController;
import com.ejisto.modules.gui.Application;
import com.ejisto.modules.gui.components.AboutPanel;
import org.springframework.context.ApplicationListener;

import javax.annotation.Resource;
import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Created by IntelliJ IDEA.
 * User: celestino
 * Date: 1/24/12
 * Time: 7:33 PM
 */
public class DialogManager implements ApplicationListener<DialogRequested> {

    private final Application application;

    public DialogManager(Application application) {
        this.application = application;
    }

    @Override
    public void onApplicationEvent(DialogRequested event) {
        try {
            JPanel view = new AboutPanel();
            DialogController controller = DialogController.Builder
                    .newInstance()
                    .withParentFrame(application)
                    .withIconKey("field.add.icon")
                    .withContent(view)
                    .resizable(event.isResizable())
                    .build();
            controller.show(true, event.getDialogSize());
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }
}
