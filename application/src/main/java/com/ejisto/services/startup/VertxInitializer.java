package com.ejisto.services.startup;

import com.ejisto.modules.vertx.VertxManager;
import com.ejisto.modules.vertx.handler.ContextHandler;
import lombok.extern.log4j.Log4j;
import org.vertx.java.core.http.HttpServer;
import org.vertx.java.core.http.RouteMatcher;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

import java.util.Collection;
import java.util.List;

import static java.net.InetAddress.getLoopbackAddress;

/**
 * Created by IntelliJ IDEA.
 * User: celestino
 * Date: 3/20/14
 * Time: 12:02 PM
 */
@Log4j
public class VertxInitializer extends BaseStartupService {

    private final Collection<ContextHandler> handlers;

    public VertxInitializer(List<ContextHandler> handlers) {
        this.handlers = handlers;
    }

    @Override
    public void execute() {
        final HttpServer server = VertxManager.getHttpServer();
        configureHttpServer(server);
        configureWebSocketServer();
        server.listen(6789, getLoopbackAddress().getHostAddress());
    }

    private void configureWebSocketServer() {
        //source: https://github.com/vert-x/vertx-examples/blob/master/src/raw/java/eventbusbridge/BridgeServer.java
        JsonArray permitted = new JsonArray();
        permitted.add(new JsonObject());
        VertxManager.getWebSocketServer().bridge(new JsonObject().putString("prefix", "/eventbus"), permitted, permitted);
    }

    private void configureHttpServer(HttpServer server) {RouteMatcher routeMatcher = new RouteMatcher();
        handlers.forEach(h -> h.addRoutes(routeMatcher));
        server.setCompressionSupported(true)
                .requestHandler(routeMatcher);
    }

    @Override
    public int getPriority() {
        return Integer.MAX_VALUE;
    }

}
