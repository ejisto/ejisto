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

package com.ejisto.modules.controller.wizard.installer;

import org.junit.Test;

import static junit.framework.Assert.assertEquals;

public class ApplicationScanningControllerTest {

    @Test
    public void testGetContextPath() {
        ApplicationScanningController applicationScanningController = new ApplicationScanningController(null, null);
        assertEquals("/simpleWarProject",
                     applicationScanningController.getContextPath("/tmp/ejisto/jetty/webapps/simpleWarProject/"));
        assertEquals("/simpleWarProject", applicationScanningController.getContextPath(
                "c:\\Windows\\Temp\\Space dir\\ejisto\\jetty\\webapps\\simpleWarProject\\"));
        assertEquals("/simpleWarProject",
                     applicationScanningController.getContextPath("/tmp/ejisto12 3/jetty/webapps/simpleWarProject/"));
        assertEquals("/simpleWarProject",
                     applicationScanningController.getContextPath("/tmp/(ejisto12 3)/jetty/webapps/simpleWarProject/"));
        assertEquals("/simpleWarProject",
                     applicationScanningController.getContextPath("/tmp/(ejisto12 3)/jetty/webapps/simpleWarProject"));
    }

}
