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

import com.ejisto.modules.dao.entities.MockedField;
import com.ejisto.modules.dao.entities.MockedFieldImpl;
import com.ejisto.modules.gui.components.helper.FieldsEditorContext;
import com.ejisto.modules.repository.MockedFieldsRepository;
import com.ejisto.modules.validation.MockedFieldValidator;
import com.ejisto.modules.vertx.handler.Boilerplate;
import com.ejisto.modules.vertx.handler.ContextHandler;
import com.ejisto.modules.web.MockedFieldRequest;
import com.ejisto.util.collector.FieldNode;
import com.ejisto.util.collector.MockedFieldCollector;
import org.vertx.java.core.MultiMap;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.http.RouteMatcher;

import java.util.Collection;
import java.util.Optional;
import java.util.function.Consumer;

import static io.netty.handler.codec.http.HttpResponseStatus.*;

/**
 * Created by IntelliJ IDEA.
 * User: celestino
 * Date: 3/28/14
 * Time: 6:27 PM
 */
public class FieldService implements ContextHandler {

    private final MockedFieldsRepository mockedFieldsRepository;

    public FieldService(MockedFieldsRepository mockedFieldsRepository) {
        this.mockedFieldsRepository = mockedFieldsRepository;
    }

    @Override
    public void addRoutes(RouteMatcher routeMatcher) {
        routeMatcher.get("/fields/grouped", request -> {
            final FieldNode node = mockedFieldsRepository.loadAll()
                    .parallelStream()
                    .filter(FieldsEditorContext.MAIN_WINDOW::isAdmitted)
                    .collect(new MockedFieldCollector());
            Boilerplate.writeOutputAsJSON(node, request.response());
        }).get("/fields/by-context-path", request -> {
            final Collection<MockedField> fields = mockedFieldsRepository.load(
                    MockedFieldRequest.requestAllClasses(request.params().get("contextPath")));
            Boilerplate.writeOutputAsJSON(fields, request.response());
        }).put("/field/validate", httpRequest -> performValidation(httpRequest.params(), httpRequest, f -> {
        })).put("/field/update",
                httpRequest -> performValidation(httpRequest.params(), httpRequest, mockedFieldsRepository::update))
        .post("/field/new", httpRequest -> {
            final MultiMap params = httpRequest.params();
            MockedField f = new MockedFieldImpl();
            f.setContextPath(params.get("contextPath"));
            f.setClassName(params.get("className"));
            f.setFieldName(params.get("fieldName"));
            f.setFieldType(params.get("fieldType"));
            f.setFieldValue(params.get("fieldValue"));
            f.setExpression(params.get("expression"));
            f.setActive(true);
            if (new MockedFieldValidator().validate(f)) {
                Boilerplate.writeOutputAsJSON(mockedFieldsRepository.insert(f), httpRequest.response());
            } else {
                Boilerplate.writeError(httpRequest, BAD_REQUEST.code(), BAD_REQUEST.reasonPhrase());
            }
        });
    }

    private void performValidation(MultiMap params, HttpServerRequest httpRequest, Consumer<MockedField> consumer) {
        Optional<MockedField> field = mockedFieldsRepository.loadOptional(params.get("contextPath"),
                                                                          params.get("className"),
                                                                          params.get("fieldName"));
        if (field.isPresent()) {
            MockedField f = field.get();
            f.setFieldValue(params.get("newValue"));
            if (new MockedFieldValidator().validate(f)) {
                consumer.accept(f);
                httpRequest.response().setStatusCode(OK.code()).end();
            } else {
                httpRequest.response().setStatusCode(BAD_REQUEST.code()).end();
            }
        } else {
            httpRequest.response().setStatusCode(NOT_FOUND.code()).end();
        }
    }

}
