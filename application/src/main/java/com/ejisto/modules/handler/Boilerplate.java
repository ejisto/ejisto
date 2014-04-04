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

import com.ejisto.constants.StringConstants;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.http.HttpServerResponse;
import org.vertx.java.core.http.RouteMatcher;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Objects;
import java.util.function.Supplier;
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
        JS("^/resources/js/(.*?\\.js)(\\?.*?)?$", "application/javascript") {
            @Override
            String addBaseDir(String path) {
                return "resources/js/" + path;
            }
        },
        CSS("^/resources/css/(.*?\\.css)(\\?.*?)?$", "text/css") {
            @Override
            String addBaseDir(String path) {
                return "resources/css/" + path;
            }
        },
        PNG("^/resources/images/(.*?\\.png)(\\?.*?)?$", "image/png") {
            @Override
            String addBaseDir(String path) {
                return "resources/images/" + path;
            }
        },
        GIF("^/resources/images/(.*?\\.gif)(\\?.*?)?$", "image/gif") {
            @Override
            String addBaseDir(String path) {
                return "resources/images/" + path;
            }
        },
        HTML("^/resources/templates/(.*?\\.html)(\\?.*?)?$", "text/html") {
            @Override
            String addBaseDir(String path) {
                return "resources/templates/" + path;
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

    static void serveResource(HttpServerResponse response, final String relativePath, String contentType) {
        Supplier<Path> resourcePathSupplier;
        if (Boolean.getBoolean(StringConstants.DEV_MODE.getValue())) {
            resourcePathSupplier = () -> Paths.get(System.getProperty("user.dir"))
                    .resolve(Paths.get("..", "src", "main", "webapp"))
                    .resolve(relativePath);
        } else {
            resourcePathSupplier = () -> Paths.get(getResourceURI(relativePath));
        }
        internalServeResource(response, resourcePathSupplier, contentType);
    }

    private static URI getResourceURI(String relativePath) {
        try {
            return INSTANCE.getClass().getResource(ROOT + relativePath).toURI();
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException(relativePath, e);
        }
    }

    private static void internalServeResource(HttpServerResponse response, Supplier<Path> resourcePathSupplier, String contentType) {
        try {
            Objects.requireNonNull(resourcePathSupplier);
            Buffer b = new Buffer(Files.readAllBytes(resourcePathSupplier.get()));
            response.putHeader("content-type", contentType).end(b);
        } catch (IOException e) {
            response.setStatusCode(HttpResponseStatus.NOT_FOUND.code());
            response.setStatusMessage(e.getMessage());
        }
    }

    static void addResourcesMatcher(RouteMatcher m) {
        Arrays.stream(ResourceType.values())
                .forEach(t -> m.getWithRegEx(t.regex, req -> serveResource(req.response(),
                                                                           extractResourceFileName(req, t),
                                                                           t.contentType)));
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
