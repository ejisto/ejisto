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

import com.ejisto.event.EventManager;
import com.ejisto.event.def.CollectedDataReceived;
import com.ejisto.modules.recorder.CollectedData;
import com.ejisto.modules.web.util.JSONUtil;
import com.ejisto.util.IOUtils;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import lombok.extern.log4j.Log4j;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import static java.net.HttpURLConnection.HTTP_INTERNAL_ERROR;
import static java.net.HttpURLConnection.HTTP_OK;

/**
 * Created by IntelliJ IDEA.
 * User: celestino
 * Date: 10/22/12
 * Time: 5:25 PM
 */
@Log4j
public class DataCollectorHandler implements HttpHandler {

    private static final String OK = "OK";
    private static final String KO = "KO";
    private final EventManager eventManager;

    public DataCollectorHandler(EventManager eventManager) {
        this.eventManager = eventManager;
    }

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        String method = httpExchange.getRequestMethod();
        log.debug(String.format("handling %s request", method));
        try (InputStream in = httpExchange.getRequestBody()) {
            recordAndReply(in, httpExchange, method);
        }
    }

    private void recordAndReply(InputStream is, HttpExchange exchange, String method) throws IOException {
        try {
            String requestBody = IOUtils.readInputStream(is, "UTF-8");
            if (method.equals("PUT")) {
                log.debug(String.format("received start notification from %s", requestBody));
            } else {
                CollectedData data = JSONUtil.decode(requestBody, CollectedData.class);
                eventManager.publishEventAndWait(new CollectedDataReceived(this, data));
            }
            exchange.sendResponseHeaders(HTTP_OK, OK.length());
            reply(exchange, OK);
        } catch (IOException | IllegalArgumentException ex) {
            exchange.sendResponseHeaders(HTTP_INTERNAL_ERROR, KO.length());
            reply(exchange, KO);
            DataCollectorHandler.log.error(exchange.getRequestURI(), ex);
        }
    }

    private void reply(HttpExchange exchange, String response) throws IOException {
        try (OutputStream out = exchange.getResponseBody()) {
            out.write(response.getBytes());
        }
    }
}
