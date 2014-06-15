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

package com.ejisto.modules.gui;

import com.ejisto.event.EventManager;
import com.ejisto.event.def.ShutdownRequest;
import com.ejisto.modules.conf.SettingsManager;

import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import static com.ejisto.constants.StringConstants.MAIN_TITLE;
import static com.ejisto.util.GuiUtils.getMessage;

public class Application extends javax.swing.JFrame {

    private static final long serialVersionUID = -3746366232127903518L;
    private final EventManager eventManager;
    private final SettingsManager settingsManager;

    public Application(EventManager eventManager,
                       SettingsManager settingsManager) {
        this.eventManager = eventManager;
        this.settingsManager = settingsManager;
    }

    public void init() {
        setTitle(settingsManager.getValue(MAIN_TITLE));
        getContentPane().add(new JLabel(getMessage("main.application.text")));
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                setVisible(false);
                eventManager.publishEvent(new ShutdownRequest(this));
            }
        });
        pack();
    }
}
