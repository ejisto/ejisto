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

package com.ejisto.modules.vertx.handler;

import com.ejisto.core.container.ContainerManager;
import com.ejisto.modules.controller.wizard.installer.workers.ApplicationScanningWorker;
import com.ejisto.modules.controller.wizard.installer.workers.FileExtractionWorker;
import com.ejisto.modules.dao.entities.WebApplicationDescriptor;
import com.ejisto.modules.repository.CustomObjectFactoryRepository;
import com.ejisto.modules.repository.MockedFieldsRepository;
import com.ejisto.modules.web.util.JSONUtil;
import com.ejisto.util.collector.MockedFieldCollector;
import lombok.extern.log4j.Log4j;
import org.apache.commons.lang3.StringUtils;
import org.vertx.java.core.Handler;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.http.RouteMatcher;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static com.ejisto.modules.vertx.VertxManager.publishEvent;
import static com.ejisto.modules.vertx.handler.Boilerplate.writeError;
import static com.ejisto.modules.vertx.handler.Boilerplate.writeOutputAsJSON;

/**
 * Created by IntelliJ IDEA.
 * User: celestino
 * Date: 4/30/14
 * Time: 8:05 AM
 */
@Log4j
public class ApplicationInstallerWizardHandler implements ContextHandler {

    private static final String SESSION_ID = "sessionID";
    private final ConcurrentMap<String, WebApplicationDescriptor> registry = new ConcurrentHashMap<>();
    private final MockedFieldsRepository mockedFieldsRepository;
    private final CustomObjectFactoryRepository customObjectFactoryRepository;
    private final ContainerManager containerManager;
    private final Handler<HttpServerRequest> uploadHandler = req -> {
        req.expectMultiPart(true);
        req.uploadHandler(fileHandler -> {
            String key = StringUtils.defaultString(req.params().get(SESSION_ID), UUID.randomUUID().toString());
            registry.putIfAbsent(key, new WebApplicationDescriptor());
            WebApplicationDescriptor session = registry.get(key);
            String fileName = fileHandler.filename();
            Path war = Paths.get(System.getProperty("java.io.tmpdir"), fileName);
            fileHandler.streamToFileSystem(war.toString());
            fileHandler.exceptionHandler(event -> writeError(req, event));
            fileHandler.endHandler(end -> {
                try {
                    session.setWarFile(war.toFile());
                    FileExtractionWorker worker = new FileExtractionWorker(session, event -> {
                        String propertyName = event.getPropertyName();
                        if (propertyName.equals("startProgress")) {
                            publishEvent("StartFileExtraction", null);
                        } else if (propertyName.equals("progressDescriptor")) {
                            publishEvent("FileExtractionProgress",
                                         JSONUtil.encode(event.getNewValue()));
                        }
                    });
                    worker.work();
                    worker.get();
                    Map<String, Object> result = new HashMap<>(2);
                    result.put(SESSION_ID, key);
                    result.put("resources", session.getIncludedJars());
                    writeOutputAsJSON(result, req.response());
                } catch (Exception e) {
                    writeError(req, e);
                }
            });
        });
    };

    public ApplicationInstallerWizardHandler(MockedFieldsRepository mockedFieldsRepository, CustomObjectFactoryRepository customObjectFactoryRepository, ContainerManager containerManager) {
        this.mockedFieldsRepository = mockedFieldsRepository;
        this.customObjectFactoryRepository = customObjectFactoryRepository;
        this.containerManager = containerManager;
    }

    @Override
    public void addRoutes(RouteMatcher routeMatcher) {
        routeMatcher.put("/application/new/upload", uploadHandler)
                .put("/application/new/:sessionID/include", req -> {
                    String sessionId = req.params().get("sessionID");
                    if(registry.containsKey(sessionId)) {
                        scanApplication(req, sessionId);
                    }
                });
    }

    private void scanApplication(HttpServerRequest req, String sessionId) {
        final WebApplicationDescriptor descriptor = registry.get(sessionId);
        Optional.ofNullable(req.params().get("resources"))
                .map(s -> s.split(","))
                .ifPresent(a -> descriptor.setWhiteList(Arrays.asList(a)));
        try {
            ApplicationScanningWorker worker = new ApplicationScanningWorker(e -> {},
                                                                             descriptor,
                                                                             mockedFieldsRepository,
                                                                             customObjectFactoryRepository,
                                                                             containerManager.getDefaultHome(),
                                                                             true);
            worker.work();
            worker.get();
            writeOutputAsJSON(descriptor.getFields().parallelStream().collect(new MockedFieldCollector()),
                              req.response());
        } catch (Exception e) {
            writeError(req, e);
            log.error("error during application scanning", e);
        }
    }
}
