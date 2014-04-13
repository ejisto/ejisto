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

import com.ejisto.core.container.ContainerManager;
import com.ejisto.modules.dao.entities.Container;
import com.ejisto.modules.repository.ContainersRepository;
import com.ejisto.modules.vertx.handler.ContextHandler;
import io.netty.handler.codec.http.HttpResponseStatus;
import lombok.Delegate;
import org.vertx.java.core.http.RouteMatcher;

import java.util.List;
import java.util.function.Function;

import static com.ejisto.modules.vertx.handler.Boilerplate.writeOutputAsJSON;
import static java.util.stream.Collectors.toList;

/**
 * Created by IntelliJ IDEA.
 * User: celestino
 * Date: 4/6/14
 * Time: 11:17 AM
 */
public class ContainerService implements ContextHandler {

    private final ContainerManager containerManager;
    private final ContainersRepository containersRepository;
    private final Function<Container, ContainerWithStatus> containerTransformer;

    public ContainerService(ContainerManager containerManager, ContainersRepository containersRepository) {
        this.containerManager = containerManager;
        this.containersRepository = containersRepository;
        this.containerTransformer = c -> new ContainerWithStatus(c, containerManager.isRunning(c.getId()));
    }

    @Override
    public void addRoutes(RouteMatcher routeMatcher) {
        routeMatcher.get("/containers/list",
                         request -> {
                             final List<ContainerWithStatus> containers = containersRepository.loadContainers()
                                     .stream()
                                     .map(containerTransformer)
                                     .collect(toList());
                             writeOutputAsJSON(containers, request.response());
                         }
        );
        routeMatcher.post("/containers/:id/:op", request -> {
            request.response()
                    .setStatusCode(HttpResponseStatus.NOT_IMPLEMENTED.code())
                    .setStatusMessage(HttpResponseStatus.NOT_IMPLEMENTED.reasonPhrase());
        });
    }

    private static final class ContainerWithStatus {
        @Delegate
        private final Container container;
        private final boolean running;

        private ContainerWithStatus(Container container, boolean running) {
            this.container = container;
            this.running = running;
        }

        public boolean isRunning() {
            return running;
        }

    }
}
