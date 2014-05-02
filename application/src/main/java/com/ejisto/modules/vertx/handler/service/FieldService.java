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
import com.ejisto.modules.gui.components.helper.FieldsEditorContext;
import com.ejisto.modules.repository.MockedFieldsRepository;
import com.ejisto.modules.validation.MockedFieldValidator;
import com.ejisto.modules.vertx.handler.Boilerplate;
import com.ejisto.modules.vertx.handler.ContextHandler;
import com.ejisto.util.collector.FieldNode;
import com.ejisto.util.collector.MockedFieldCollector;
import org.vertx.java.core.MultiMap;
import org.vertx.java.core.http.RouteMatcher;

import java.util.Optional;

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
        }).put("/field/update", httpRequest -> {
            final MultiMap params = httpRequest.params();
            Optional<MockedField> field = mockedFieldsRepository.loadOptional(params.get("contextPath"),
                                                         params.get("className"),
                                                         params.get("fieldName"));
            if(field.isPresent()) {
                MockedField f = field.get();
                f.setFieldValue(params.get("newValue"));
                if(new MockedFieldValidator().validate(f)) {
                    mockedFieldsRepository.update(f);
                    httpRequest.response().setStatusCode(OK.code()).end();
                } else {
                    httpRequest.response().setStatusCode(BAD_REQUEST.code()).end();
                }
            } else {
                httpRequest.response().setStatusCode(NOT_FOUND.code()).end();
            }
        });
    }

}
