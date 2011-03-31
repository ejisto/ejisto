/*
 * Ejisto, a powerful developer assistant
 *
 * Copyright (C) 2010-2011  Celestino Bellone
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

import com.ejisto.core.classloading.SharedClassLoader;
import org.apache.log4j.Logger;
import org.jdesktop.swingx.JXErrorPane;
import org.jdesktop.swingx.error.ErrorInfo;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import javax.swing.*;
import java.io.File;
import java.io.OutputStream;
import java.util.logging.Level;

public class Main {

    static {
        File baseDir = new File(System.getProperty("user.home"), ".ejisto");
        if (!baseDir.exists() && !baseDir.mkdir()) {
            JOptionPane.showConfirmDialog(null, "Ejisto doesn't have permissions to create its homedir. Please check system configuration.", "error",
                                          JOptionPane.OK_OPTION, JOptionPane.ERROR_MESSAGE);
            throw new ExceptionInInitializerError("Cannot create home dir. Exiting.");
        }
        System.setProperty("ejisto.home", baseDir.getAbsolutePath());
        System.setProperty("java.net.preferIPv4Stack", "true");
    }

    private static Logger logger = Logger.getLogger(Main.class);

    /*
     * to start application from an IDE (like Intellij Idea)
     * add -Dejisto.agent.jar.path=/path/to/agent.jar to vm parameters
     * in launch configuration panel
     * and set working directory to:
     * /path/to/ejisto/application/target
     */
    public static void main(String[] args) {
        try {
            //tanks to David Van Couvering for tip to disable this annoying "feature"
            //http://davidvancouvering.blogspot.com/2007/10/quiet-time-and-how-to-suppress-derbylog.html
            System.setProperty("derby.stream.error.field", "com.ejisto.core.launcher.Main.DEV_NULL");
            System.getProperties().list(System.out);
            logger.info("Starting Ejisto...");
            logger.info("setting dynamic ClassLoader");
            Thread.currentThread().setContextClassLoader(SharedClassLoader.getInstance());
            logger.info("initializing Spring framework");
            AbstractApplicationContext context = new ClassPathXmlApplicationContext("/spring-context.xml");
            context.registerShutdownHook();
            ApplicationController controller = context.getBean("applicationController", ApplicationController.class);
            logger.info("starting application... enjoy ejisto!!");
            controller.startup();
        } catch (Exception e) {
            JXErrorPane.showDialog(null, new ErrorInfo("Startup error", "Startup failed", e.getMessage(), "SEVERE", e, Level.SEVERE, null));
            System.exit(-1);
        }
    }

    public static final OutputStream DEV_NULL = new OutputStream() {
        public void write(int b) { }
    };
}
