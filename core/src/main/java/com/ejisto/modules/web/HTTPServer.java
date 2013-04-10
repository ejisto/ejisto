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

package com.ejisto.modules.web;

import com.ejisto.constants.StringConstants;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static com.ejisto.util.IOUtils.findFirstAvailablePort;

/**
 * Created by IntelliJ IDEA.
 * User: celestino
 * Date: 6/26/12
 * Time: 4:24 PM
 */
public class HTTPServer {

    private final ConcurrentMap<String, HttpHandler> handlersMap;
    private final HttpServer server;

    public HTTPServer(List<RemoteRequestHandler> handlers) throws IOException {
        this.handlersMap = toHandlersMap(handlers);
        int port = findFirstAvailablePort(1706);
        server = HttpServer.create(new InetSocketAddress(port), 1024);
        for (RemoteRequestHandler handler : handlers) {
            server.createContext(handler.getContextPath(), handler);
        }
        server.setExecutor(null);
        server.start();
        System.setProperty(StringConstants.HTTP_LISTEN_PORT.getValue(), String.valueOf(port));
    }

    public boolean createContext(String contextPath, HttpHandler handler) {
        if (handlersMap.containsKey(contextPath)) {
            return false;
        }
        HttpHandler existing = handlersMap.putIfAbsent(contextPath, handler);
        if (existing != null) {
            handlersMap.put(contextPath, existing);
            return false;
        }
        server.createContext(contextPath, handler);
        return true;
    }

    public void removeContext(String contextPath) {
        server.removeContext(contextPath);
        handlersMap.remove(contextPath);
    }

    private static ConcurrentMap<String, HttpHandler> toHandlersMap(List<RemoteRequestHandler> remoteRequestHandlers) {
        ConcurrentMap<String, HttpHandler> result = new ConcurrentHashMap<>();
        for (RemoteRequestHandler handler : remoteRequestHandlers) {
            result.put(handler.getContextPath(), handler);
        }
        return result;
    }
}
