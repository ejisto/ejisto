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

package com.ejisto.modules.vertx.handler.internal;

import com.ejisto.modules.repository.MockedFieldsRepository;
import com.ejisto.modules.vertx.handler.Boilerplate;
import com.ejisto.modules.vertx.handler.ContextHandler;
import com.ejisto.modules.web.MockedFieldRequest;
import com.ejisto.modules.web.util.JSONUtil;
import com.ejisto.modules.web.util.MockedFieldsJSONUtil;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.http.RouteMatcher;

import static com.ejisto.constants.StringConstants.CTX_GET_MOCKED_FIELD;
import static com.ejisto.constants.StringConstants.CTX_NEWLY_CREATED_FIELDS;

/**
 * Created by IntelliJ IDEA.
 * User: celestino
 * Date: 7/21/14
 * Time: 6:19 PM
 */
public class MockedFieldHandler implements ContextHandler {

    private final MockedFieldsRepository mockedFieldsRepository;

    public MockedFieldHandler(MockedFieldsRepository mockedFieldsRepository) {
        this.mockedFieldsRepository = mockedFieldsRepository;
    }

    @Override
    public void addRoutes(RouteMatcher routeMatcher) {
        routeMatcher.get(CTX_NEWLY_CREATED_FIELDS.getValue(), this::getNewlyCreatedFields)
                    .get(CTX_GET_MOCKED_FIELD.getValue(), this::getExistingField);
    }

    private void getNewlyCreatedFields(HttpServerRequest request) {
        String result = MockedFieldsJSONUtil.encodeMockedFields(mockedFieldsRepository.getRecentlyCreatedFields());
        Boilerplate.writeOutput(result, request.response());
    }

    private void getExistingField(HttpServerRequest request) {
        request.bodyHandler(buffer -> {
            MockedFieldRequest req = JSONUtil.decode(buffer.toString(), MockedFieldRequest.class);
            String result = MockedFieldsJSONUtil.encodeMockedFields(mockedFieldsRepository.load(req));
            Boilerplate.writeOutput(result, request.response());
        });
    }


}
