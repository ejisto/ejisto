/*
 * Ejisto, a powerful developer assistant
 *
 * Copyright (C) 2010-2012  Celestino Bellone
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
import lombok.extern.log4j.Log4j;
import org.jdesktop.swingx.JXErrorPane;
import org.jdesktop.swingx.error.ErrorInfo;
import org.springframework.context.support.GenericXmlApplicationContext;

import javax.swing.*;
import java.util.logging.Level;

import static com.ejisto.util.GuiUtils.getRootThrowable;

/**
 * Created by IntelliJ IDEA.
 * User: celestino
 * Date: 2/16/12
 * Time: 7:10 PM
 */
@Log4j
public class Launcher {
    int launch() {
        try {
            //tanks to David Van Couvering for describing how to disable this annoying "feature"
            //http://davidvancouvering.blogspot.com/2007/10/quiet-time-and-how-to-suppress-derbylog.html
            System.setProperty("derby.stream.error.field", "com.ejisto.core.launcher.Main.DEV_NULL");
            System.getProperties().list(System.out);
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            log.info("Starting Ejisto...");
            log.info("setting dynamic ClassLoader");
            Thread.currentThread().setContextClassLoader(SharedClassLoader.getInstance());
            log.info("initializing Spring framework");
            GenericXmlApplicationContext context = new GenericXmlApplicationContext();
            context.getEnvironment().setActiveProfiles("server");
            context.load("classpath:/spring-context.xml");
            context.refresh();
            context.registerShutdownHook();
            ApplicationController controller = context.getBean("applicationController", ApplicationController.class);
            log.info("starting application... enjoy ejisto!!");
            controller.startup();
            return 0;
        } catch (Exception e) {
            JXErrorPane.showDialog(null,
                                   new ErrorInfo("Startup error", "Startup failed", null, "SEVERE", getRootThrowable(e),
                                                 Level.SEVERE,
                                                 null));
            log.error("startup failed", e);
            return -1;
        }
    }
}
