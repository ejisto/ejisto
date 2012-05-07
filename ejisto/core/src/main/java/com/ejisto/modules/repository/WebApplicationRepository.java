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

package com.ejisto.modules.repository;

import com.ejisto.core.container.WebApplication;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static ch.lambdaj.Lambda.forEach;

/**
 * Created by IntelliJ IDEA.
 * User: celestino
 * Date: 4/11/11
 * Time: 7:05 PM
 */
public class WebApplicationRepository {
    private final ConcurrentMap<String, Map<String, WebApplication<?>>> webApplications;

    public WebApplicationRepository() {
        webApplications = new ConcurrentHashMap<String, Map<String, WebApplication<?>>>();
    }

    public void registerWebApplication(String containerId, WebApplication<?> webApplication) {
        if (!webApplications.containsKey(containerId)) {
            webApplications.putIfAbsent(containerId, new HashMap<String, WebApplication<?>>());
        }
        webApplications.get(containerId).put(webApplication.getWebApplicationContextPath(), webApplication);
    }

    public WebApplication<?> getRegisteredWebApplication(String containerId, String context) {
        return webApplications.get(containerId).get(context);
    }

    public void unregisterWebApplication(String containerId, String context) {
        webApplications.get(containerId).remove(context);
    }

    public void containerShutdown(String containerId) {
        changeWebApplicationsStatus(containerId, WebApplication.Status.STOPPED);
    }

    public void containerStartup(String containerId) {
        changeWebApplicationsStatus(containerId, WebApplication.Status.STARTED);
    }

    public Map<String, List<WebApplication<?>>> getInstalledWebApplications() {
        Map<String, List<WebApplication<?>>> applications = new TreeMap<String, List<WebApplication<?>>>();
        List<WebApplication<?>> serverApplications;
        for (String serverId : webApplications.keySet()) {
            serverApplications = new ArrayList<WebApplication<?>>(webApplications.get(serverId).values());
            applications.put(serverId, serverApplications);
        }
        return applications;
    }

    private void changeWebApplicationsStatus(String containerId, WebApplication.Status status) {
        Map<String, WebApplication<?>> map = webApplications.get(containerId);
        if (map == null) {
            return;
        }
        Collection<WebApplication<?>> containerApplications = map.values();
        forEach(containerApplications).setStatus(status);
    }
}
