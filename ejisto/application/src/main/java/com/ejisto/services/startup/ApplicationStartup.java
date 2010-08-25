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

package com.ejisto.services.startup;

import static com.ejisto.util.GuiUtils.centerOnScreen;

import javax.annotation.Resource;

import com.ejisto.modules.gui.Application;

public class ApplicationStartup extends BaseStartupService {

	@Resource
	private Application application;

	@Override
	public void execute() {
		application.init();
		centerOnScreen(application);
		application.setVisible(true);
	}

}
