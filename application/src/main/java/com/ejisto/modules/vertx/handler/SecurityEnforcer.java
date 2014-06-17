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

package com.ejisto.modules.vertx.handler;

import com.ejisto.constants.StringConstants;
import com.ejisto.modules.web.util.DigestUtil;
import io.netty.handler.codec.http.Cookie;
import io.netty.handler.codec.http.DefaultCookie;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.ServerCookieEncoder;
import org.vertx.java.core.MultiMap;
import org.vertx.java.core.http.HttpHeaders;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.http.RouteMatcher;

import java.util.Optional;
import java.util.UUID;

/**
 * Created by IntelliJ IDEA.
 * User: celestino
 * Date: 6/16/14
 * Time: 7:26 AM
 */
public class SecurityEnforcer extends RouteMatcher {

    static final String SECURITY_TOKEN = ")]}',\n";
    static final String XSRF_TOKEN = "XSRF-TOKEN";
    static final String X_REQUESTED_WITH = "X-Requested-With";

    private final String token;

    public SecurityEnforcer() {
        token = DigestUtil.sha256Digest(UUID.randomUUID().toString());
    }

    SecurityEnforcer(String token) {
        this.token = token;
    }

    @Override
    public void handle(HttpServerRequest request) {

        final MultiMap headers = request.headers();
        Optional<String> xRequestedWith = Optional.ofNullable(headers.get(X_REQUESTED_WITH))
                .filter("XMLHttpRequest"::equals);

        if (xRequestedWith.isPresent()) {
            if (!isDevModeActive()) {
                request.response().write(SECURITY_TOKEN);
            }
            Optional<String> header = Optional.ofNullable(headers.get(XSRF_TOKEN))
                    .filter(token::equals);
            if(!header.isPresent()) {
                Boilerplate.writeError(request, HttpResponseStatus.FORBIDDEN.code(), HttpResponseStatus.FORBIDDEN.reasonPhrase());
                return;
            }
        }

        if("/".equals(request.path())) {
            Cookie cookie = new DefaultCookie(XSRF_TOKEN, token);
            cookie.setDomain("localhost");
            cookie.setPath("/");
            request.response().headers().set(HttpHeaders.SET_COOKIE, ServerCookieEncoder.encode(cookie));
        }
        super.handle(request);
    }

    private boolean isDevModeActive() {
        return Boolean.getBoolean(StringConstants.DEV_MODE.getValue());
    }

}
