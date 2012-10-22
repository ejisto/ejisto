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

import com.ejisto.core.ApplicationException;

import javax.swing.*;
import java.io.File;
import java.io.OutputStream;


public class Main {

    static {
        File baseDir = new File(System.getProperty("user.home"), ".ejisto");
        if (!baseDir.exists() && !baseDir.mkdir()) {
            JOptionPane.showConfirmDialog(null,
                                          "Ejisto doesn't have permissions to create its home directory. Please check system configuration.",
                                          "error", JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE);
            throw new ApplicationException("Cannot create home dir. Exiting.");
        }
        System.setProperty("ejisto.home", baseDir.getAbsolutePath());
        System.setProperty("java.net.preferIPv4Stack", "true");
    }

    /*
     * to start application from an IDE (like Intellij Idea)
     * add -Dejisto.agent.jar.path=/path/to/agent.jar to vm parameters
     * in launch configuration panel
     * it is also required to set working directory to:
     * /path-to-ejisto-project/application/target
     */
    public static void main(String[] args) {
        int status = new Launcher().launch();
        if (status < 0) {
            //something went wrong...
            System.out.printf("exit status: %s", status);
            System.exit(status);
        }
    }

    public static final OutputStream DEV_NULL = new OutputStream() {
        public void write(int b) { }
    };
}
