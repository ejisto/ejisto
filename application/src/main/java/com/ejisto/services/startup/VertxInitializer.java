package com.ejisto.services.startup;

import com.ejisto.modules.vertx.VertxManager;
import com.ejisto.modules.vertx.handler.ContextHandler;
import com.ejisto.modules.vertx.handler.SecurityEnforcer;
import lombok.extern.log4j.Log4j;
import org.vertx.java.core.http.HttpServer;
import org.vertx.java.core.http.RouteMatcher;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;

import static com.ejisto.constants.StringConstants.HTTP_LISTEN_PORT;
import static com.ejisto.util.IOUtils.findFirstAvailablePort;
import static java.net.InetAddress.getLoopbackAddress;

/**
 * Created by IntelliJ IDEA.
 * User: celestino
 * Date: 3/20/14
 * Time: 12:02 PM
 */
@Log4j
public class VertxInitializer extends BaseStartupService {

    private final static Predicate<ContextHandler> IS_INTERNAL = ContextHandler::isInternal;

    private final Collection<ContextHandler> handlers;

    public VertxInitializer(List<ContextHandler> handlers) {
        this.handlers = handlers;
    }

    @Override
    public void execute() {
        final HttpServer server = VertxManager.getHttpServer();
        configureHttpServer(server);
        configureWebSocketServer();
        final String hostAddress = getLoopbackAddress().getHostAddress();
        server.listen(6789, hostAddress);
        final HttpServer internalServer = VertxManager.getInternalHttpServer();
        configureInternalHttpServer(internalServer);
        int port = findFirstAvailablePort(1706);
        internalServer.listen(port, hostAddress);
        System.setProperty(HTTP_LISTEN_PORT.getValue(), String.valueOf(port));
    }

    private void configureWebSocketServer() {
        //source: https://github.com/vert-x/vertx-examples/blob/master/src/raw/java/eventbusbridge/BridgeServer.java
        JsonArray permitted = new JsonArray();
        permitted.add(new JsonObject());
        VertxManager.getWebSocketServer().bridge(new JsonObject().putString("prefix", "/eventbus"), permitted, permitted);
    }

    private void configureHttpServer(HttpServer server) {
        fillWithRouteMatchers(server, new SecurityEnforcer(), IS_INTERNAL.negate());
    }

    private void configureInternalHttpServer(HttpServer server) {
        fillWithRouteMatchers(server, new RouteMatcher(), IS_INTERNAL);
    }

    private void fillWithRouteMatchers(HttpServer server, RouteMatcher routeMatcher, Predicate<ContextHandler> filter) {
        handlers.stream()
                .filter(filter)
                .forEach(h -> h.addRoutes(routeMatcher));
        server.setCompressionSupported(true)
                .requestHandler(routeMatcher);
    }

    @Override
    public int getPriority() {
        return Integer.MAX_VALUE - 1;
    }

}
