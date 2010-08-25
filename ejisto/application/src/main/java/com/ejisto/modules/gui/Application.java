/*
 * Copyright 2010 Celestino Bellone
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions
 * and limitations under the License.
 */
package com.ejisto.modules.gui;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.annotation.Resource;

import com.ejisto.constants.StringConstants;
import com.ejisto.event.EventManager;
import com.ejisto.event.def.ShutdownRequest;
import com.ejisto.modules.conf.SettingsManager;

public class Application extends javax.swing.JFrame {

    private static final long serialVersionUID = -3746366232127903518L;
    @Resource
    private EventManager eventManager;
    @Resource
    private SettingsManager settingsManager;
    private MainRootPane rootPane;

    public Application() {
    }

    public void init() {
        setTitle(settingsManager.getValue(StringConstants.MAIN_TITLE));
        rootPane = new MainRootPane();
        setRootPane(rootPane);
        setMinimumSize(new java.awt.Dimension(600, 300));
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                setVisible(false);
                eventManager.publishEvent(new ShutdownRequest(this));
            }
        });
        pack();
    }

    public void log(String message) {
        rootPane.log(message);
    }

}
