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

package com.ejisto.core.launcher;

import org.apache.log4j.Logger;
import org.jdesktop.swingx.JXErrorPane;
import org.jdesktop.swingx.error.ErrorInfo;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import javax.swing.*;
import java.io.File;
import java.util.logging.Level;


public class Main {

    static {
        File baseDir = new File(System.getProperty("user.home"), ".ejisto");
        if (!baseDir.exists() && !baseDir.mkdir()) {
            JOptionPane.showConfirmDialog(null, "Ejisto doesn't have permissions to create its homedir. Please check system configuration.", "error", JOptionPane.OK_OPTION, JOptionPane.ERROR_MESSAGE);
            throw new ExceptionInInitializerError("Cannot create home dir. Exiting.");
        }
        System.setProperty("ejisto.home", baseDir.getAbsolutePath());
    }

    private static Logger logger = Logger.getLogger(Main.class);

    public static void main(String[] args) {
        try {
            logger.info("Starting Ejisto...");
            System.setProperty("org.eclipse.jetty.util.log.class", "com.ejisto.core.jetty.logging.JettyLogger");
            AbstractApplicationContext context = new ClassPathXmlApplicationContext("/spring-context.xml");
            context.registerShutdownHook();
            ApplicationController controller = context.getBean("applicationController", ApplicationController.class);
            controller.startup();
        } catch (Exception e) {
            JXErrorPane.showDialog(null, new ErrorInfo("Startup error", "Startup failed", e.getMessage(), "SEVERE", e, Level.SEVERE, null));
        }
    }
}
