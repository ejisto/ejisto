/*
 * Ejisto, a powerful developer assistant
 *
 * Copyright (C) 2010  Celestino Bellone
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
import com.ejisto.event.def.ShutdownRequest;
import com.ejisto.modules.conf.SettingsManager;

import javax.annotation.Resource;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Date;

import static com.ejisto.constants.StringConstants.*;
import static com.ejisto.util.GuiUtils.getAction;
import static com.ejisto.util.GuiUtils.getMessage;

public class Application extends javax.swing.JFrame {

	private static final long serialVersionUID = -3746366232127903518L;
	@Resource
	private EventManager eventManager;
	@Resource
	private SettingsManager settingsManager;
	private MainRootPane rootPane;
    private boolean ready;
    private ArrayList<String> pendingMessages = new ArrayList<String>();

	public Application() {
	}

	public void init() {
		setTitle(settingsManager.getValue(MAIN_TITLE));
		rootPane = new MainRootPane();
		setRootPane(rootPane);
		setMinimumSize(new Dimension(700,350));
		Dimension size = new Dimension(settingsManager.getIntValue(APPLICATION_WIDTH), settingsManager.getIntValue(APPLICATION_HEIGHT));
		setSize(size);
		setPreferredSize(size);
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				saveSettings();
				setVisible(false);
				eventManager.publishEvent(new ShutdownRequest(this));
			}
		});
		pack();
		ready=true;
	}
	
	public void log(String message) {
		if(ready) {
		    for (String oldMessage : pendingMessages) rootPane.log(oldMessage);
		    if(!pendingMessages.isEmpty())pendingMessages.clear();
		    rootPane.log(message);
		} else {
		    pendingMessages.add(message);
		}
	}

    public void setStatusBarMessage(String messageKey, boolean error) {
        if(ready) {
            rootPane.setStatusBarMessage(messageKey, error);
        }
    }

	public void onServerStatusChange(ChangeServerStatus event) {
		boolean shutdown = event.getCommand() == Command.SHUTDOWN;
		if (shutdown)
			rootPane.log(getMessage("jetty.shutdown.log", new Date()));
		getAction(START_JETTY.getValue()).setEnabled(shutdown);
		getAction(STOP_JETTY.getValue()).setEnabled(!shutdown);
		rootPane.toggleDisplayServerLog(shutdown);
	}
	
	public void onApplicationDeploy() {
		rootPane.onPropertyChange();
	}
	
	public void onWebAppContextStatusChange() {
        rootPane.onPropertyChange();
    }
	
	public boolean isReady() {
	    return this.ready;
	}
	
	private void saveSettings() {
		settingsManager.putValue(APPLICATION_WIDTH, getWidth());
		settingsManager.putValue(APPLICATION_HEIGHT, getHeight());
	}

}
