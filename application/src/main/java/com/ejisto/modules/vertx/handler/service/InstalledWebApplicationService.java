/*
 * Ejisto, a powerful developer assistant
 *
 * Copyright (C) 2010-2014 Celestino Bellone
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

package com.ejisto.modules.vertx.handler.service;

import com.ejisto.core.container.WebApplication;
import com.ejisto.modules.dao.local.LocalWebApplicationDescriptorDao;
import com.ejisto.modules.repository.WebApplicationRepository;
import com.ejisto.modules.vertx.handler.ContextHandler;
import org.vertx.java.core.http.RouteMatcher;

import java.util.Collection;
import java.util.stream.Collectors;

import static com.ejisto.modules.vertx.handler.Boilerplate.writeOutputAsJSON;

/**
 * Created by IntelliJ IDEA.
 * User: celestino
 * Date: 4/6/14
 * Time: 11:05 AM
 */
public class InstalledWebApplicationService implements ContextHandler {

    private final WebApplicationRepository webApplicationRepository;
    private final LocalWebApplicationDescriptorDao webApplicationDescriptorDao;

    public InstalledWebApplicationService(WebApplicationRepository webApplicationRepository,
                                          LocalWebApplicationDescriptorDao webApplicationDescriptorDao) {
        this.webApplicationRepository = webApplicationRepository;
        this.webApplicationDescriptorDao = webApplicationDescriptorDao;
    }

    @Override
    public void addRoutes(RouteMatcher routeMatcher) {
        routeMatcher.get("/webApplications/list",
                         request -> writeOutputAsJSON(getInstalledWebApplications(),
                                                      request.response()));
    }

    private Collection<InstalledWebApplication> getInstalledWebApplications() {
        return webApplicationDescriptorDao.loadAll().stream()
                .map(d -> {
                    final WebApplication.Status status = webApplicationRepository
                            .getRegisteredWebApplication(d.getContainerId(), d.getContextPath())
                            .map(WebApplication::getStatus).orElse(WebApplication.Status.UNKNOWN);
                    return new InstalledWebApplication(d.getContextPath(), d.getContainerId(), status);
                }).collect(Collectors.toList());
    }

    private static final class InstalledWebApplication {
        private final String contextPath;
        private final String containerID;
        private final WebApplication.Status status;


        private InstalledWebApplication(String contextPath, String containerID, WebApplication.Status status) {
            this.contextPath = contextPath;
            this.containerID = containerID;
            this.status = status;
        }

        public String getContextPath() {
            return contextPath;
        }

        public String getContainerID() {
            return containerID;
        }

        public WebApplication.Status getStatus() {
            return status;
        }

        public boolean isRunning() {
            return status == WebApplication.Status.STARTED;
        }

        public boolean isStopped() {
            return status == WebApplication.Status.STOPPED;
        }
    }

}
