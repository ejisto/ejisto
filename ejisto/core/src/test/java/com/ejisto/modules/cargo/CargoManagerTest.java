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

package com.ejisto.modules.cargo;

import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.*;

/**
 * Created by IntelliJ IDEA.
 * User: celestino
 * Date: 2/18/11
 * Time: 7:24 PM
 */
public class CargoManagerTest {
    @Test
    @Ignore
    public void testDownloadAndInstall() throws Exception {
        CargoManager manager = new CargoManager();
        try {
            String tmp = System.getProperty("java.io.tmpdir");
            String home = manager.downloadAndInstall(
                    "http://mirror.switch.ch/mirror/apache/dist/tomcat/tomcat-7/v7.0.8/bin/apache-tomcat-7.0.8.tar.gz",
                    tmp);
            System.out.println(home);
            assertNotNull(home);
            assertTrue(home.startsWith(tmp));
        } catch (IOException e) {
            fail(e.getMessage());
        }
    }
}
