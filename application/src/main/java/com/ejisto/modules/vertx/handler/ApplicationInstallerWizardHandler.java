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

import com.ejisto.constants.StringConstants;
import com.ejisto.core.container.ContainerManager;
import com.ejisto.event.EventManager;
import com.ejisto.event.def.ApplicationInstallFinalization;
import com.ejisto.modules.cargo.NotInstalledException;
import com.ejisto.modules.controller.wizard.installer.workers.ApplicationScanningWorker;
import com.ejisto.modules.controller.wizard.installer.workers.FileExtractionWorker;
import com.ejisto.modules.dao.entities.MockedField;
import com.ejisto.modules.dao.entities.WebApplicationDescriptor;
import com.ejisto.modules.dao.local.LocalWebApplicationDescriptorDao;
import com.ejisto.modules.repository.ContainersRepository;
import com.ejisto.modules.repository.CustomObjectFactoryRepository;
import com.ejisto.modules.repository.MockedFieldsRepository;
import com.ejisto.modules.vertx.handler.service.ApplicationInstallerRegistry;
import com.ejisto.modules.web.util.JSONUtil;
import com.ejisto.util.collector.MockedFieldCollector;
import com.fasterxml.jackson.core.type.TypeReference;
import io.netty.handler.codec.http.HttpResponseStatus;
import lombok.extern.log4j.Log4j;
import org.apache.commons.lang3.StringUtils;
import org.vertx.java.core.Handler;
import org.vertx.java.core.MultiMap;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.http.RouteMatcher;

import java.beans.PropertyChangeListener;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import static com.ejisto.constants.StringConstants.GUI_TASK_EXCEPTION_PROPERTY;
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
    private static final TypeReference<List<MockedField>> MF_LIST_TYPE_REFERENCE = new TypeReference<List<MockedField>>() {
    };
    private final MockedFieldsRepository mockedFieldsRepository;
    private final CustomObjectFactoryRepository customObjectFactoryRepository;
    private final ContainerManager containerManager;
    private final LocalWebApplicationDescriptorDao webApplicationDescriptorDao;
    private final EventManager eventManager;
    private final ContainersRepository containersRepository;

    private final Handler<HttpServerRequest> uploadHandler = req -> {
        req.expectMultiPart(true);
        req.uploadHandler(fileHandler -> {
            String key = StringUtils.defaultString(req.params().get(SESSION_ID), UUID.randomUUID().toString());
            ApplicationInstallerRegistry.putDescriptorIfAbsent(key, new WebApplicationDescriptor());
            WebApplicationDescriptor session = ApplicationInstallerRegistry.getDescriptor(key).orElseThrow(IllegalStateException::new);
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

    private final Handler<HttpServerRequest> scanHandler = req -> {
        String sessionId = req.params().get(ApplicationInstallerWizardHandler.SESSION_ID);
        if (ApplicationInstallerRegistry.isPresent(sessionId)) {
            scanApplication(req, sessionId);
        }
    };

    public ApplicationInstallerWizardHandler(MockedFieldsRepository mockedFieldsRepository,
                                             CustomObjectFactoryRepository customObjectFactoryRepository,
                                             ContainerManager containerManager,
                                             LocalWebApplicationDescriptorDao webApplicationDescriptorDao,
                                             EventManager eventManager,
                                             ContainersRepository containersRepository) {
        this.mockedFieldsRepository = mockedFieldsRepository;
        this.customObjectFactoryRepository = customObjectFactoryRepository;
        this.containerManager = containerManager;
        this.webApplicationDescriptorDao = webApplicationDescriptorDao;
        this.eventManager = eventManager;
        this.containersRepository = containersRepository;
    }

    @Override
    public void addRoutes(RouteMatcher routeMatcher) {
        routeMatcher.put("/application/new/upload", uploadHandler)
                .put("/application/new/:" + SESSION_ID + "/include", scanHandler)
                .post("/application/new/:" + SESSION_ID + "/publish", req -> {
                    final MultiMap params = req.params();
                    req.bodyHandler(buffer -> {
                        try {
                            handleApplicationPublish(req, params, buffer);
                        } catch (Exception e) {
                            writeError(req, e);
                        }
                    });
                });
    }

    private void handleApplicationPublish(HttpServerRequest req, MultiMap params, Buffer buffer) throws NotInstalledException {
        List<MockedField> newFields = JSONUtil.decode(buffer.toString(), MF_LIST_TYPE_REFERENCE);
        String sessionID = params.get(SESSION_ID);
        final Optional<WebApplicationDescriptor> optional = ApplicationInstallerRegistry.getDescriptor(sessionID);
        if (!optional.isPresent()) {
            writeError(req, HttpResponseStatus.BAD_REQUEST.code(), "invalid sessionID");
            return;
        }
        WebApplicationDescriptor descriptor = optional.get();
        final Collection<MockedField> descriptorFields = descriptor.getFields();
        newFields.forEach(field -> descriptorFields.stream()
                .filter(f -> f.getComparisonKey().equals(field.getComparisonKey()))
                .findFirst()
                .ifPresent(f -> {
                    f.setActive(true);
                    f.setFieldValue(field.getFieldValue());
                    f.setExpression(field.getExpression());
                }));
        descriptor.getFields().stream().forEach(f -> f.setContextPath(descriptor.getContextPath()));
        mockedFieldsRepository.deleteContext(descriptor.getContextPath());
        mockedFieldsRepository.insert(descriptorFields);
        webApplicationDescriptorDao.insert(descriptor);
        String containerID = Optional.ofNullable(descriptor.getContainerId()).orElse(
                StringConstants.DEFAULT_CONTAINER_ID.getValue());
        eventManager.publishEvent(
                new ApplicationInstallFinalization(this, descriptor, containersRepository.loadContainer(containerID)));
        req.response().setStatusCode(HttpResponseStatus.OK.code()).end();
    }

    private void scanApplication(HttpServerRequest req, String sessionId) {
        final WebApplicationDescriptor descriptor = ApplicationInstallerRegistry.getDescriptor(sessionId).orElseThrow(IllegalStateException::new);
        Optional.ofNullable(req.params().get("resources"))
                .map(s -> s.split(","))
                .ifPresent(a -> descriptor.setWhiteList(Arrays.asList(a)));
        try {
            PropertyChangeListener listener = e -> {
                if(e.getPropertyName().equals(GUI_TASK_EXCEPTION_PROPERTY.getValue())) {
                    writeError(req, (Throwable)e.getNewValue());
                }
            };
            ApplicationScanningWorker worker = new ApplicationScanningWorker(listener,
                                                                             descriptor,
                                                                             mockedFieldsRepository,
                                                                             customObjectFactoryRepository,
                                                                             containerManager.getDefaultHome(),
                                                                             true);
            worker.addTaskExecutionListener(e -> {
                switch (e) {
                    case DONE:
                        writeOutputAsJSON(descriptor.getFields()
                                                  .parallelStream()
                                                  .collect(new MockedFieldCollector()),
                                          req.response()
                        );
                        break;
                    case CANCELED:
                        writeError(req, HttpResponseStatus.GONE.code(), "task has been canceled");
                        break;
                    default:
                        break;
                }
            });
            worker.work();
        } catch (Exception e) {
            writeError(req, e);
            log.error("error during application scanning", e);
        }
    }
}
