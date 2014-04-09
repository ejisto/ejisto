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

package com.ejisto.modules.handler.service;

import com.ejisto.modules.handler.ContextHandler;
import com.ejisto.modules.repository.WebApplicationRepository;
import org.vertx.java.core.http.RouteMatcher;

import static com.ejisto.modules.handler.Boilerplate.writeOutputAsJSON;

/**
 * Created by IntelliJ IDEA.
 * User: celestino
 * Date: 4/6/14
 * Time: 11:05 AM
 */
public class InstalledWebApplicationService implements ContextHandler {

    private final WebApplicationRepository webApplicationRepository;

    public InstalledWebApplicationService(WebApplicationRepository webApplicationRepository) {
        this.webApplicationRepository = webApplicationRepository;
    }

    @Override
    public void addRoutes(RouteMatcher routeMatcher) {
        routeMatcher.get("/webApplications", request -> {
            writeOutputAsJSON(webApplicationRepository.getInstalledWebApplications(), request.response());
        });
    }
}
