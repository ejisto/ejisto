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

package com.ejisto.core.container;

import com.ejisto.modules.cargo.NotInstalledException;
import com.ejisto.modules.dao.entities.Container;
import com.ejisto.modules.dao.entities.WebApplicationDescriptor;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: celestino
 * Date: 3/10/11
 * Time: 8:28 PM
 */
public interface ContainerManager {
    String downloadAndInstall(String urlToString, String folder) throws IOException;

    boolean isServerRunning();

    boolean isRunning(String containerId);

    boolean startDefault() throws NotInstalledException;

    boolean stopDefault() throws NotInstalledException;

    void stopAllRunningContainers() throws NotInstalledException;

    boolean start(Container container);

    boolean stop(Container container);

    boolean deployToDefaultContainer(WebApplicationDescriptor webApplicationDescriptor) throws NotInstalledException;

    boolean deploy(WebApplicationDescriptor webApplicationDescriptor, Container container);

    boolean undeploy(String containerId, String contextPath);

    boolean undeployFromDefaultContainer(String contextPath) throws NotInstalledException;

    boolean startWebApplication(String containerId, String contextPath);

    boolean stopWebApplication(String containerId, String contextPath);

    boolean startWebApplicationOnDefaultServer(String contextPath) throws NotInstalledException;

    boolean stopWebApplicationOnDefaultServer(String contextPath) throws NotInstalledException;

    String getDefaultHome() throws NotInstalledException;

    String getHome(Container container);

    Container startStandaloneInstance(Map<String, String> additionalJavaSystemProperties, List<WebApplicationDescriptor> webApplications) throws NotInstalledException, IOException;


}
