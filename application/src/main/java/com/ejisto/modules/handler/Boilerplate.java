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
import com.ejisto.modules.web.util.JSONUtil;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.http.HttpHeaders;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by IntelliJ IDEA.
 * User: celestino
 * Date: 3/21/14
 * Time: 5:05 PM
 */
public final class Boilerplate {

    private static final Boilerplate INSTANCE = new Boilerplate();
    private static final String ROOT = "/";
    private static final ResourcePathSupplier DEV_SUPPLIER = relativePath ->
            Paths.get(System.getProperty("user.dir"))
                    .resolve(Paths.get("..", "src", "main", "webapp"))
                    .resolve(relativePath);
    private static final ResourcePathSupplier DEFAULT_SUPPLIER = relativePath -> Paths.get(
            getResourceURI(relativePath));
    private static boolean DEV_MODE = Boolean.getBoolean(StringConstants.DEV_MODE.getValue());

    private Boilerplate() {
    }

    public static <T> void writeOutputAsJSON(T output, HttpServerResponse response) {
        String result = JSONUtil.encode(output);
        response.putHeader(HttpHeaders.CONTENT_LENGTH, String.valueOf(result.length()))
                .putHeader(HttpHeaders.CONTENT_TYPE, "application/json")
                .write(result)
                .end();
    }

    static void serveTemplate(HttpServerResponse response, String relativePath) {
        serveResource(response, relativePath, "text/html");
    }

    static void serveResource(HttpServerResponse response, final String relativePath, String contentType) {
        internalServeResource(response,
                              DEV_MODE ? DEV_SUPPLIER.get(relativePath) : DEFAULT_SUPPLIER.get(relativePath),
                              contentType);
    }

    private static URI getResourceURI(String relativePath) {
        try {
            return INSTANCE.getClass().getResource(ROOT + relativePath).toURI();
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException(relativePath, e);
        }
    }

    private static void internalServeResource(HttpServerResponse response, Path resourcePath, String contentType) {
        try {
            Objects.requireNonNull(resourcePath);
            final byte[] resourceBytes = Files.readAllBytes(resourcePath);
            Buffer b = new Buffer(resourceBytes);
            response.putHeader(HttpHeaders.CONTENT_TYPE, contentType)
                    .putHeader(HttpHeaders.CONTENT_LENGTH, String.valueOf(resourceBytes.length))
                    .write(b)
                    .end();
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

    @FunctionalInterface
    private interface ResourcePathSupplier {
        Path get(String relativePath);
    }

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
}
