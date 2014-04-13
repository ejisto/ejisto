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

package com.ejisto.modules.vertx;

import org.vertx.java.core.Vertx;
import org.vertx.java.core.VertxFactory;
import org.vertx.java.core.http.HttpServer;
import org.vertx.java.core.sockjs.SockJSServer;

import java.util.concurrent.atomic.AtomicReference;

/**
 * Created by IntelliJ IDEA.
 * User: celestino
 * Date: 4/11/14
 * Time: 8:20 AM
 */
public final class VertxManager {

    private static final VertxManager INSTANCE = new VertxManager();
    private final Vertx vertx;
    private final AtomicReference<HttpServer> httpServer = new AtomicReference<>();
    private final AtomicReference<SockJSServer> webSocketServer = new AtomicReference<>();

    private VertxManager() {
        vertx = VertxFactory.newVertx();
    }

    private HttpServer initHttpServer() {
        HttpServer server = httpServer.get();
        if(server == null) {
            httpServer.compareAndSet(null, vertx.createHttpServer());
            return httpServer.get();
        }
        return server;
    }

    private SockJSServer initWebSocketServer() {
        SockJSServer server = webSocketServer.get();
        if(server == null) {
            webSocketServer.compareAndSet(null, vertx.createSockJSServer(initHttpServer()));
            return webSocketServer.get();
        }
        return server;
    }

    public static HttpServer getHttpServer() {
        return INSTANCE.initHttpServer();
    }

    public static SockJSServer getWebSocketServer() {
        return INSTANCE.initWebSocketServer();
    }

    public static void publishEvent(String address, String message) {
        INSTANCE.vertx.eventBus().publish(address, message);
    }

    public static void registerClient() {
        //INSTANCE.webSocketServer.bridge()
    }


}
