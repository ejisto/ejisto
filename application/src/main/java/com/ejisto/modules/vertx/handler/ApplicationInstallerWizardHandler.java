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

import com.ejisto.modules.controller.wizard.installer.workers.FileExtractionWorker;
import com.ejisto.modules.dao.entities.WebApplicationDescriptor;
import com.ejisto.modules.web.util.JSONUtil;
import org.apache.commons.lang3.StringUtils;
import org.vertx.java.core.Handler;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.http.RouteMatcher;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static com.ejisto.modules.vertx.VertxManager.publishEvent;

/**
 * Created by IntelliJ IDEA.
 * User: celestino
 * Date: 4/30/14
 * Time: 8:05 AM
 */
public class ApplicationInstallerWizardHandler implements ContextHandler {

    private static final String SESSION_ID = "sessionID";
    private final ConcurrentMap<String, WebApplicationDescriptor> registry = new ConcurrentHashMap<>();

    private final Handler<HttpServerRequest> uploadHandler = req -> {
        req.expectMultiPart(true);
        req.uploadHandler(fileHandler -> {
            String key = StringUtils.defaultString(req.params().get(SESSION_ID), UUID.randomUUID().toString());
            registry.putIfAbsent(key, new WebApplicationDescriptor());
            WebApplicationDescriptor session = registry.get(key);
            String fileName = fileHandler.filename();
            Path war = Paths.get(System.getProperty("java.io.tmpdir"), fileName);
            fileHandler.streamToFileSystem(war.toString());
            fileHandler.exceptionHandler(event -> Boilerplate.writeError(req, event));
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
                    Boilerplate.writeOutputAsJSON(result, req.response());
                } catch (Exception e) {
                    Boilerplate.writeError(req, e);
                }
            });
        });
    };

    @Override
    public void addRoutes(RouteMatcher routeMatcher) {
        routeMatcher.put("/application/new/upload", uploadHandler);
    }
}
