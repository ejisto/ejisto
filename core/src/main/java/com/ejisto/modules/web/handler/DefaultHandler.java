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

import com.ejisto.modules.web.RemoteRequestHandler;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by IntelliJ IDEA.
 * User: celestino
 * Date: 7/4/12
 * Time: 10:59 AM
 */
public class DefaultHandler implements RemoteRequestHandler {

    private static final byte[] GREETINGS = "Hi, I'm ejisto. How can I help you? :)".getBytes();

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        try (OutputStream os = httpExchange.getResponseBody()) {
            httpExchange.sendResponseHeaders(200, GREETINGS.length);
            os.write(GREETINGS);
            os.close();
        }
    }

    @Override
    public String getContextPath() {
        return "/";
    }
}
