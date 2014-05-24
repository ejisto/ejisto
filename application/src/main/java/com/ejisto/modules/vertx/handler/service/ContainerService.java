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
import com.ejisto.modules.cargo.util.DownloadFailed;
import com.ejisto.modules.conf.SettingsManager;
import com.ejisto.modules.dao.entities.Container;
import com.ejisto.modules.dao.entities.ContainerType;
import com.ejisto.modules.executor.BackgroundTask;
import com.ejisto.modules.executor.TaskManager;
import com.ejisto.modules.repository.ContainersRepository;
import com.ejisto.modules.vertx.handler.ContextHandler;
import lombok.Delegate;
import lombok.extern.log4j.Log4j;
import org.apache.commons.lang3.StringUtils;
import org.vertx.java.core.MultiMap;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.http.RouteMatcher;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import static com.ejisto.constants.StringConstants.*;
import static com.ejisto.modules.vertx.handler.Boilerplate.writeError;
import static com.ejisto.modules.vertx.handler.Boilerplate.writeOutputAsJSON;
import static io.netty.handler.codec.http.HttpResponseStatus.*;
import static java.util.stream.Collectors.toList;

/**
 * Created by IntelliJ IDEA.
 * User: celestino
 * Date: 4/6/14
 * Time: 11:17 AM
 */
@Log4j
public class ContainerService implements ContextHandler {

    private final ContainerManager containerManager;
    private final SettingsManager settingsManager;
    private final ContainersRepository containersRepository;
    private final Function<Container, ContainerWithStatus> containerTransformer;
    private final TaskManager taskManager;

    public ContainerService(ContainerManager containerManager,
                            SettingsManager settingsManager,
                            ContainersRepository containersRepository,
                            TaskManager taskManager) {

        this.containerManager = containerManager;
        this.settingsManager = settingsManager;
        this.containersRepository = containersRepository;
        this.taskManager = taskManager;
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
                })
                .get("/containers/supported", request -> writeOutputAsJSON(ContainerType.values(), request.response()))
                .put("/containers/install", this::downloadAndInstallContainer)
                .post("/containers/:id/:op", request -> request.response()
                        .setStatusCode(NOT_IMPLEMENTED.code())
                        .setStatusMessage(NOT_IMPLEMENTED.reasonPhrase()))
        ;
    }

    private void downloadAndInstallContainer(HttpServerRequest request) {
        final MultiMap params = request.params();
        String url = StringUtils.defaultIfEmpty(params.get("url"),
                                               settingsManager.getValue("container.default.url"));

        final Optional<Container> defaultContainer = Optional.ofNullable(params.get("defaultContainer"))
                .filter(Boolean::parseBoolean)
                .map(x -> containersRepository.safeLoadContainer(DEFAULT_CONTAINER_ID.getValue()));

        if (defaultContainer.isPresent()) {
            writeError(request, BAD_REQUEST.code(), BAD_REQUEST.reasonPhrase());
            return;
        }

        taskManager.addNewTask(new BackgroundTask<>(() -> {
            try {
                final ContainerType containerType;
                if(!StringUtils.isEmpty(params.get("containerType"))) {
                    containerType = ContainerType.valueOf(params.get("containerType"));
                } else {
                    containerType = ContainerType.fromCargoId(DEFAULT_CARGO_ID.getValue());
                }
                containerManager.downloadAndInstall(url, System.getProperty(CONTAINERS_HOME_DIR.getValue()), containerType);
                settingsManager.putValue(DEFAULT_CONTAINER_DOWNLOAD_URL, url);
                request.response().setStatusCode(OK.code()).setStatusMessage(OK.reasonPhrase()).end();
            } catch (DownloadFailed e) {
                log.debug("got DownloadFailed exception", e);
                writeError(request, NOT_FOUND.code(), NOT_FOUND.reasonPhrase());
            } catch (Exception e) {
                log.debug("got Exception", e);
                writeError(request, e);
            }
            return null;
        }));
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
