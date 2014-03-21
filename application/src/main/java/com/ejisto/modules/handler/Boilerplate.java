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

package com.ejisto.modules.handler;

import io.netty.handler.codec.http.HttpResponseStatus;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.http.HttpServerResponse;
import org.vertx.java.core.http.RouteMatcher;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by IntelliJ IDEA.
 * User: celestino
 * Date: 3/21/14
 * Time: 5:05 PM
 */
final class Boilerplate {

    private static final Boilerplate INSTANCE = new Boilerplate();
    private static final String ROOT = "/";

    private enum ResourceType {
        JS("/resources/js/(.*?\\.js)(\\?.*?)?$", "application/javascript") {
            @Override
            String addBaseDir(String path) {
                return "js/" + path;
            }
        },
        CSS("/resources/css/(.*?\\.css)(\\?.*?)$", "text/css") {
            @Override
            String addBaseDir(String path) {
                return "css/" + path;
            }
        };
        private final String regex;
        private final Pattern pattern;
        private final String contentType;

        ResourceType(String regex, String contentType) {
            this.regex = regex;
            this.pattern = Pattern.compile(regex);
            this.contentType = contentType;
        }

        abstract String addBaseDir(String path);


    }


    private Boilerplate() {
    }

    static void serveTemplate(HttpServerResponse response, String relativePath) {
        serveResource(response, relativePath, "text/html");
    }

    static void serveResource(HttpServerResponse response, String relativePath, String contentType) {
        try {
            Objects.requireNonNull(relativePath);
            final URI template = INSTANCE.getClass().getResource(ROOT + relativePath).toURI();
            Buffer b = new Buffer(Files.readAllBytes(Paths.get(template)));
            response.putHeader("content-type", contentType).end(b);
        } catch (IOException | URISyntaxException e) {
            response.setStatusCode(HttpResponseStatus.NOT_FOUND.code());
            response.setStatusMessage(HttpResponseStatus.NOT_FOUND.reasonPhrase());
        }
    }

    static RouteMatcher resourcesMatcher() {
        RouteMatcher m = new RouteMatcher();
        Arrays.stream(ResourceType.values())
                .forEach(t -> m.getWithRegEx(t.regex,
                                             req -> serveResource(req.response(), extractResourceFileName(req, t),
                                                                  t.contentType)));
        return m;
    }

    static void sendRedirect(HttpServerResponse response, String target) {
        response.setStatusCode(HttpResponseStatus.FOUND.code()).putHeader("Location", target).end();
    }

    private static String extractResourceFileName(HttpServerRequest request, ResourceType resourceType) {
        final Matcher matcher = resourceType.pattern.matcher(request.path());
        if (matcher.matches()) {
            final String resource = matcher.group(1);
            Objects.requireNonNull(resource);
            return resourceType.addBaseDir(resource);
        }
        throw new IllegalArgumentException("invalid URL");
    }
}
