/*
 * Ejisto, a powerful developer assistant
 *
 * Copyright (C) 2010-2013 Celestino Bellone
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

package com.ejisto.modules.web.handler;

import com.ejisto.modules.dao.entities.MockedField;
import com.ejisto.modules.repository.MockedFieldsRepository;
import com.ejisto.modules.web.MockedFieldRequest;
import com.ejisto.modules.web.RemoteRequestHandler;
import com.ejisto.modules.web.util.ConfigurationManager;
import com.ejisto.modules.web.util.JSONUtil;
import com.ejisto.modules.web.util.MockedFieldsJSONUtil;
import com.ejisto.util.IOUtils;
import com.sun.net.httpserver.HttpExchange;
import lombok.extern.log4j.Log4j;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;

import static com.ejisto.constants.StringConstants.CTX_GET_MOCKED_FIELD;
import static com.ejisto.constants.StringConstants.GET_NEWLY_CREATED_FIELDS_REQUEST;

/**
 * Created by IntelliJ IDEA.
 * User: celestino
 * Date: 7/4/12
 * Time: 11:00 AM
 */
@Log4j
public class MockedFieldRequestHandler implements RemoteRequestHandler {

    private final MockedFieldsRepository mockedFieldsRepository;

    public MockedFieldRequestHandler(MockedFieldsRepository mockedFieldsRepository) {
        this.mockedFieldsRepository = mockedFieldsRepository;
    }

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        try (OutputStream os = httpExchange.getResponseBody()) {
            String requestBody = IOUtils.readInputStream(httpExchange.getRequestBody(), "UTF-8");
            Collection<MockedField> found;
            if(httpExchange.getRequestURI().toString().endsWith(GET_NEWLY_CREATED_FIELDS_REQUEST.getValue())) {
                found = mockedFieldsRepository.getRecentlyCreatedFields();
            } else {
                MockedFieldRequest request = JSONUtil.decode(requestBody, MockedFieldRequest.class);
                found = mockedFieldsRepository.load(request);
            }
            String response = MockedFieldsJSONUtil.encodeMockedFields(found);
            httpExchange.sendResponseHeaders(200, response.length());
            os.write(response.getBytes(ConfigurationManager.UTF_8));
        } catch (Exception e) {
            log.error("error during mockedFieldRequest handling", e);
        }
    }

    @Override
    public String getContextPath() {
        return CTX_GET_MOCKED_FIELD.getValue();
    }
}
