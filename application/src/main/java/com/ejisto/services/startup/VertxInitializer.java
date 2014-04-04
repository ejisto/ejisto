package com.ejisto.services.startup;

import com.ejisto.modules.handler.ContextHandler;
import lombok.extern.log4j.Log4j;
import org.vertx.java.core.VertxFactory;
import org.vertx.java.core.http.HttpServer;
import org.vertx.java.core.http.RouteMatcher;

import java.net.InetAddress;
import java.util.Collection;
import java.util.List;

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
        String host = InetAddress.getLoopbackAddress().getHostAddress();
        log.info("hostname: "+host);
        final HttpServer server = VertxFactory.newVertx().createHttpServer();
        RouteMatcher routeMatcher = new RouteMatcher();
        handlers.forEach(h -> h.addRoutes(routeMatcher));
        server.requestHandler(routeMatcher).listen(6789, host);
    }

    @Override
    public int getPriority() {
        return Integer.MAX_VALUE;
    }

}
