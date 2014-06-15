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

package com.ejisto.core.launcher;

import com.ejisto.core.classloading.SharedClassLoader;
import com.ejisto.core.configuration.RootBundle;
import lombok.extern.log4j.Log4j;
import se.jbee.inject.Injector;
import se.jbee.inject.bootstrap.Bootstrap;

import javax.swing.*;

import static se.jbee.inject.Dependency.dependency;

/**
 * Created by IntelliJ IDEA.
 * User: celestino
 * Date: 2/16/12
 * Time: 7:10 PM
 */
@Log4j
class Launcher {
    int launch() {
        try {
            Thread.setDefaultUncaughtExceptionHandler((t, e) -> {
                Launcher.log.error(String.format("Unexpected exception on thread %s", t.getName()), e);
            });
            System.getProperties().list(System.out);
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            log.info("Starting Ejisto...");
            log.info("setting dynamic ClassLoader");
            Thread.currentThread().setContextClassLoader(SharedClassLoader.getInstance());
            log.info("initializing Silk di");
            Injector injector = Bootstrap.injector(RootBundle.class);
            ApplicationController controller = injector.resolve(dependency(ApplicationController.class));
            log.info("starting application... enjoy ejisto!!");
            controller.startup();
            return 0;
        } catch (Exception e) {
            log.error("startup failed", e);
            return -1;
        }
    }

}
