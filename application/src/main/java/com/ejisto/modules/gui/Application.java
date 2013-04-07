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
import com.ejisto.event.def.ChangeServerStatus;
import com.ejisto.event.def.ChangeServerStatus.Command;
import com.ejisto.event.def.LogMessage;
import com.ejisto.event.def.ShutdownRequest;
import com.ejisto.event.ApplicationEventDispatcher;
import com.ejisto.modules.conf.SettingsManager;
import com.ejisto.modules.executor.BackgroundTask;
import com.ejisto.modules.executor.TaskManager;
import com.ejisto.modules.repository.ContainersRepository;
import com.ejisto.modules.repository.MockedFieldsRepository;

import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Date;
import java.util.concurrent.Callable;

import static com.ejisto.constants.StringConstants.*;
import static com.ejisto.util.GuiUtils.*;

public class Application extends javax.swing.JFrame {

    private static final long serialVersionUID = -3746366232127903518L;
    private final EventManager eventManager;
    private final SettingsManager settingsManager;
    private final TaskManager taskManager;
    private final MockedFieldsRepository mockedFieldsRepository;
    private final ContainersRepository containersRepository;
    private final ApplicationEventDispatcher applicationEventDispatcher;

    public Application(EventManager eventManager,
                       SettingsManager settingsManager,
                       TaskManager taskManager,
                       MockedFieldsRepository mockedFieldsRepository,
                       ContainersRepository containersRepository,
                       ApplicationEventDispatcher applicationEventDispatcher) {
        this.eventManager = eventManager;
        this.settingsManager = settingsManager;
        this.taskManager = taskManager;
        this.mockedFieldsRepository = mockedFieldsRepository;
        this.containersRepository = containersRepository;
        this.applicationEventDispatcher = applicationEventDispatcher;
    }

    public void init() {
        setIconImage(getIcon("application.icon").getImage());
        setTitle(settingsManager.getValue(MAIN_TITLE));
        setRootPane(new MainRootPane(mockedFieldsRepository, containersRepository, applicationEventDispatcher));
        setMinimumSize(new Dimension(700, 500));
        Dimension size = new Dimension(settingsManager.getIntValue(APPLICATION_WIDTH),
                                       settingsManager.getIntValue(APPLICATION_HEIGHT));
        setSize(size);
        if (settingsManager.getBooleanValue(APPLICATION_MAXIMIZED)) {
            setExtendedState(MAXIMIZED_BOTH);
        }
        setPreferredSize(size);
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                taskManager.addNewTask(new BackgroundTask<>(new Callable<Void>() {
                    @Override
                    public Void call() throws Exception {
                        settingsManager.putValue(APPLICATION_WIDTH, Application.this.getWidth());
                        settingsManager.putValue(APPLICATION_HEIGHT, Application.this.getHeight());
                        return null;
                    }
                }));

            }
        });
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                saveSettings();
                setVisible(false);
                eventManager.publishEvent(new ShutdownRequest(this));
            }
        });
        pack();
    }

    public void onServerStatusChange(ChangeServerStatus event) {
        boolean shutdown = event.getCommand() == Command.SHUTDOWN;
        if (shutdown) {
            eventManager.publishEvent(new LogMessage(this, getMessage("default.server.shutdown.log", new Date()),
                                                     event.getContainerId()));
        }
        getAction(START_CONTAINER.getValue()).setEnabled(shutdown);
        getAction(STOP_CONTAINER.getValue()).setEnabled(!shutdown);
    }

    private void saveSettings() {
        settingsManager.putValue(APPLICATION_WIDTH, getWidth());
        settingsManager.putValue(APPLICATION_HEIGHT, getHeight());
        settingsManager.putValue(APPLICATION_MAXIMIZED, getState() == MAXIMIZED_BOTH);
    }

}
