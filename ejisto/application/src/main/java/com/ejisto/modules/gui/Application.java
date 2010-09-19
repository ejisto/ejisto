/*******************************************************************************
 * Copyright 2010 Celestino Bellone
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package com.ejisto.modules.gui;

import static com.ejisto.constants.StringConstants.APPLICATION_HEIGHT;
import static com.ejisto.constants.StringConstants.APPLICATION_WIDTH;
import static com.ejisto.constants.StringConstants.MAIN_TITLE;
import static com.ejisto.constants.StringConstants.START_JETTY;
import static com.ejisto.constants.StringConstants.STOP_JETTY;
import static com.ejisto.util.GuiUtils.getAction;
import static com.ejisto.util.GuiUtils.getMessage;

import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Date;

import javax.annotation.Resource;

import com.ejisto.event.EventManager;
import com.ejisto.event.def.ChangeServerStatus;
import com.ejisto.event.def.ChangeServerStatus.Command;
import com.ejisto.event.def.ShutdownRequest;
import com.ejisto.modules.conf.SettingsManager;

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
		Dimension size = new Dimension(settingsManager.getIntValue(APPLICATION_WIDTH), settingsManager.getIntValue(APPLICATION_HEIGHT));
		setMinimumSize(size);
		setSize(size);
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
