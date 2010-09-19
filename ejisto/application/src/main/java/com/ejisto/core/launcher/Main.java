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

package com.ejisto.core.launcher;

import java.io.File;

import javax.swing.JOptionPane;

import org.apache.log4j.Logger;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;


public class Main {
	
	static {
		File baseDir = new File(System.getProperty("user.home"), ".ejisto");
		if(!baseDir.exists() && !baseDir.mkdir()) {
			JOptionPane.showConfirmDialog(null, "Ejisto doesn't have permissions to create its homedir. Please check system configuration.", "error", JOptionPane.OK_OPTION, JOptionPane.ERROR_MESSAGE);
			throw new ExceptionInInitializerError("Cannot create home dir. Exiting.");
		}
		System.setProperty("ejisto.home", baseDir.getAbsolutePath());
	}
	
    private static Logger logger = Logger.getLogger(Main.class);

    public static void main(String[] args) {
        logger.info("Starting Ejisto...");
        System.setProperty("org.eclipse.jetty.util.log.class", "com.ejisto.core.jetty.logging.JettyLogger");
        AbstractApplicationContext context = new ClassPathXmlApplicationContext("/spring-context.xml");
        context.registerShutdownHook();
        ApplicationController controller = context.getBean("applicationController", ApplicationController.class);
        controller.startup();
    }
}
