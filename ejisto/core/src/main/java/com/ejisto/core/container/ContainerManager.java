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

package com.ejisto.core.container;

import com.ejisto.modules.cargo.NotInstalledException;
import com.ejisto.modules.dao.entities.Container;
import com.ejisto.modules.dao.entities.WebApplicationDescriptor;

import java.io.IOException;

/**
 * Created by IntelliJ IDEA.
 * User: celestino
 * Date: 3/10/11
 * Time: 8:28 PM
 */
public interface ContainerManager {
    String downloadAndInstall(String urlToString, String folder) throws IOException;

    boolean isServerRunning();

    boolean startDefault() throws NotInstalledException;

    boolean stopDefault() throws NotInstalledException;

    boolean start(Container container) throws NotInstalledException;

    boolean stop(Container container) throws NotInstalledException;

    boolean deployToDefaultContainer(WebApplicationDescriptor webApplicationDescriptor) throws NotInstalledException;

    boolean deploy(WebApplicationDescriptor webApplicationDescriptor, Container container) throws NotInstalledException;

    String getDefaultHome() throws NotInstalledException;

    String getHome(Container container);
}
