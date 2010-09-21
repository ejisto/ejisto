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

package com.ejisto.services.startup;

import com.ejisto.modules.gui.Application;
import org.apache.log4j.Logger;

import javax.annotation.Resource;

import static com.ejisto.util.GuiUtils.centerOnScreen;

public class ApplicationStartup extends BaseStartupService {
    private static final Logger logger = Logger.getLogger(ApplicationStartup.class);
	
    @Resource
	private Application application;

	@Override
	public void execute() {
		logger.info("executing ApplicationStartup");
	    application.init();
		centerOnScreen(application);
		application.setVisible(true);
	}

}
