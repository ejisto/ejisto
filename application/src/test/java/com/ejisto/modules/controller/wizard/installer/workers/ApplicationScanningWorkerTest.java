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

package com.ejisto.modules.controller.wizard.installer.workers;

import com.ejisto.modules.controller.wizard.installer.ApplicationScanningController;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ApplicationScanningWorkerTest {

    private ApplicationScanningController controller;

    @Before
    public void init() {
        controller = new ApplicationScanningController(null, "", null, null, null);
    }

    @Test
    public void testGetContextPath() {
        ApplicationScanningWorker worker = new ApplicationScanningWorker(controller, null, null,null, "", true);
        assertEquals("/simpleWarProject",
                     worker.getContextPath("/tmp/ejisto/jetty/webapps/simpleWarProject/"));
        assertEquals("/simpleWarProject", worker.getContextPath(
                "c:\\Windows\\Temp\\Space dir\\ejisto\\jetty\\webapps\\simpleWarProject\\"));
        assertEquals("/simpleWarProject",
                     worker.getContextPath("/tmp/ejisto12 3/jetty/webapps/simpleWarProject/"));
        assertEquals("/simpleWarProject",
                     worker.getContextPath("/tmp/(ejisto12 3)/jetty/webapps/simpleWarProject/"));
        assertEquals("/simpleWarProject",
                     worker.getContextPath("/tmp/(ejisto12 3)/jetty/webapps/simpleWarProject"));
    }

}
