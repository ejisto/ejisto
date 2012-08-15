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
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.springframework.beans.factory.InitializingBean;

import javax.annotation.Resource;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Map;

import static com.ejisto.util.IOUtils.findFirstAvailablePort;

/**
 * Created by IntelliJ IDEA.
 * User: celestino
 * Date: 6/26/12
 * Time: 4:24 PM
 */
public class HTTPServer implements InitializingBean {

    private static final HTTPServer INSTANCE = new HTTPServer();

    @Resource(name = "httpHandlers") private Map<String, HttpHandler> handlersMap;

    public static HTTPServer getInstance() {
        return INSTANCE;
    }

    private HTTPServer() {
    }

    @Override
    public void afterPropertiesSet() throws IOException {
        int port = findFirstAvailablePort(1706);
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 1024);
        for (Map.Entry<String, HttpHandler> entry : handlersMap.entrySet()) {
            server.createContext(entry.getKey(), entry.getValue());
        }
        server.setExecutor(null);
        server.start();
        System.setProperty(StringConstants.HTTP_LISTEN_PORT.getValue(), String.valueOf(port));
    }
}
