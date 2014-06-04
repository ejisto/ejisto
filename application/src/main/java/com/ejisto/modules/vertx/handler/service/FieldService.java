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
import com.ejisto.modules.dao.entities.WebApplicationDescriptor;
import com.ejisto.modules.gui.components.helper.FieldsEditorContext;
import com.ejisto.modules.repository.MockedFieldsRepository;
import com.ejisto.modules.validation.MockedFieldValidator;
import com.ejisto.modules.vertx.handler.Boilerplate;
import com.ejisto.modules.vertx.handler.ContextHandler;
import com.ejisto.modules.web.MockedFieldRequest;
import com.ejisto.util.LambdaUtil;
import com.ejisto.util.collector.FieldNode;
import com.ejisto.util.collector.MockedFieldCollector;
import org.vertx.java.core.Handler;
import org.vertx.java.core.MultiMap;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.http.RouteMatcher;

import java.util.Collection;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

import static com.ejisto.constants.StringConstants.*;
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
        routeMatcher.get("/fields/grouped", getFieldsGrouped())
                .get("/fields/by-context-path", getFieldsByContextPath())
                .put("/field/validate", validateField())
                .put("/field/validate/for/:sessionID", validateWizardField())
                .put("/field/update", updateField())
                .post("/field/new", insertField());
    }

    private Handler<HttpServerRequest> insertField() {
        return httpRequest -> {
            final MultiMap params = httpRequest.params();
            MockedField f = new MockedFieldImpl();
            f.setContextPath(params.get(PARAM_CONTEXT_PATH.getValue()));
            f.setClassName(params.get(PARAM_CLASS_NAME.getValue()));
            f.setFieldName(params.get(PARAM_FIELD_NAME.getValue()));
            f.setFieldType(params.get(PARAM_FIELD_TYPE.getValue()));
            f.setFieldValue(params.get(PARAM_FIELD_VALUE.getValue()));
            f.setExpression(params.get(PARAM_EXPRESSION.getValue()));
            f.setActive(true);
            if (new MockedFieldValidator().validate(f)) {
                Boilerplate.writeOutputAsJSON(mockedFieldsRepository.insert(f), httpRequest.response());
            } else {
                Boilerplate.writeError(httpRequest, BAD_REQUEST.code(), BAD_REQUEST.reasonPhrase());
            }
        };
    }

    private Handler<HttpServerRequest> validateWizardField() {
        return httpRequest -> performValidation(httpRequest.params(), httpRequest, f -> {}, getFieldFromWizardRegistry(httpRequest));
    }

    private Supplier<Optional<MockedField>> getFieldFromWizardRegistry(HttpServerRequest httpRequest) {
        return () -> {
            final MultiMap params = httpRequest.params();
            final Predicate<MockedField> predicate = LambdaUtil.findField(
                    params.get(PARAM_CONTEXT_PATH.getValue()),
                    params.get(PARAM_CLASS_NAME.getValue()),
                    params.get(PARAM_FIELD_NAME.getValue()));
            return ApplicationInstallerRegistry.getDescriptor(params.get("sessionID"))
                    .map(WebApplicationDescriptor::getFields)
                    .flatMap(c -> c.stream().filter(predicate).findFirst());

        };
    }

    private Handler<HttpServerRequest> updateField() {
        return httpRequest -> performValidation(httpRequest.params(), httpRequest, mockedFieldsRepository::update,
                                                getFieldFromRepository(httpRequest));
    }

    private Handler<HttpServerRequest> validateField() {
        return httpRequest -> performValidation(httpRequest.params(), httpRequest, f -> {
        }, getFieldFromRepository(httpRequest));
    }

    private Supplier<Optional<MockedField>> getFieldFromRepository(HttpServerRequest httpRequest) {
        return () -> {
            final MultiMap params = httpRequest.params();
            return mockedFieldsRepository.loadOptional(
                    params.get(PARAM_CONTEXT_PATH.getValue()),
                    params.get(PARAM_CLASS_NAME.getValue()),
                    params.get(PARAM_FIELD_NAME.getValue()));
        };
    }

    private void performValidation(MultiMap params, HttpServerRequest httpRequest,
                                   Consumer<MockedField> consumer,
                                   Supplier<Optional<MockedField>> fieldFinder) {
        Optional<MockedField> field = fieldFinder.get();
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

    private Handler<HttpServerRequest> getFieldsByContextPath() {
        return request -> {
            final Collection<MockedField> fields = mockedFieldsRepository.load(
                    MockedFieldRequest.requestAllClasses(request.params().get("contextPath")));
            Boilerplate.writeOutputAsJSON(fields, request.response());
        };
    }

    private Handler<HttpServerRequest> getFieldsGrouped() {
        return request -> {
            final FieldNode node = mockedFieldsRepository.loadAll()
                    .parallelStream()
                    .filter(FieldsEditorContext.MAIN_WINDOW::isAdmitted)
                    .collect(new MockedFieldCollector());
            Boilerplate.writeOutputAsJSON(node, request.response());
        };
    }

}
