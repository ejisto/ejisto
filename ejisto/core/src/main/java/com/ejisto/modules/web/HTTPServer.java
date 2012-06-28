/*
 * Ejisto, a powerful developer assistant
 *
 * Copyright (C) 2010-2012  Celestino Bellone
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

package com.ejisto.modules.web;

import com.ejisto.constants.StringConstants;
import com.ejisto.modules.dao.entities.MockedField;
import com.ejisto.modules.repository.MockedFieldsRepository;
import com.ejisto.modules.web.util.JSONUtil;
import com.ejisto.util.IOUtils;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.List;

import static com.ejisto.util.IOUtils.findFirstAvailablePort;

/**
 * Created by IntelliJ IDEA.
 * User: celestino
 * Date: 6/26/12
 * Time: 4:24 PM
 */
public class HTTPServer {

    private static final HTTPServer INSTANCE = new HTTPServer();

    public static HTTPServer getInstance() {
        return INSTANCE;
    }

    private HTTPServer() {
        try {
            int port = findFirstAvailablePort(1706);
            HttpServer server = HttpServer.create(new InetSocketAddress(port), 1024);
            server.createContext("/", new DefaultHandler());
            server.createContext("/getField", new MockedFieldRequestHandler());
            server.setExecutor(null);
            server.start();
            System.setProperty(StringConstants.HTTP_LISTEN_PORT.getValue(), String.valueOf(port));
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    class DefaultHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange httpExchange) throws IOException {
            String response = "Hi, I'm ejisto. How can I help you? :)";
            httpExchange.sendResponseHeaders(200, response.length());
            OutputStream os = httpExchange.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }
    }

    class MockedFieldRequestHandler implements HttpHandler {

        @Override
        public void handle(HttpExchange httpExchange) throws IOException {
            ObjectMapper mapper = new ObjectMapper();
            mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            ObjectReader fieldReader = mapper.reader(MockedField.class);
            String requestBody = IOUtils.readInputStream(httpExchange.getRequestBody(), "UTF-8");
            MockedFieldRequest request = JSONUtil.decodeMockedFieldRequest(requestBody);
            List<MockedField> found = MockedFieldsRepository.getInstance().load(request);
            String response = JSONUtil.encodeMockedFields(found);
            httpExchange.sendResponseHeaders(200, response.length());
            OutputStream os = httpExchange.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }
    }
}
