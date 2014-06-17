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
import io.netty.handler.codec.http.Cookie;
import io.netty.handler.codec.http.DefaultCookie;
import io.netty.handler.codec.http.ServerCookieEncoder;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.vertx.java.core.MultiMap;
import org.vertx.java.core.http.CaseInsensitiveMultiMap;
import org.vertx.java.core.http.HttpHeaders;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.http.HttpServerResponse;

import static org.junit.Assert.*;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class SecurityEnforcerTest {

    private static final String PATH = "/someAction";
    private static final String NSA_PROOF_TOKEN = "addatorna' baffone!";
    private SecurityEnforcer enforcer;
    @Mock private HttpServerRequest serverRequest;
    @Mock private HttpServerResponse serverResponse;
    @Mock private MultiMap responseHeaders;

    @Before
    public void setUp() throws Exception {
        enforcer = new SecurityEnforcer(NSA_PROOF_TOKEN);
        when(serverRequest.method()).thenReturn("GET");
        when(serverRequest.params()).thenReturn(new CaseInsensitiveMultiMap());
        when(serverRequest.headers()).thenReturn(new CaseInsensitiveMultiMap());
        when(serverRequest.response()).thenReturn(serverResponse);
        when(serverResponse.headers()).thenReturn(responseHeaders);
        when(serverResponse.write(anyString())).thenReturn(serverResponse);
        enforcer.get(PATH, r -> r.response().write("OK").end());
    }

    @Test
    public void testProductionHandle() throws Exception {
        MultiMap headers = new CaseInsensitiveMultiMap();
        headers.add(SecurityEnforcer.X_REQUESTED_WITH, "XMLHttpRequest");
        headers.add(SecurityEnforcer.XSRF_TOKEN, NSA_PROOF_TOKEN);
        when(serverRequest.headers()).thenReturn(headers);
        when(serverRequest.path()).thenReturn(PATH);
        System.setProperty(StringConstants.DEV_MODE.getValue(), "false");
        enforcer.handle(serverRequest);
        verify(serverResponse).write(SecurityEnforcer.SECURITY_TOKEN);
        verify(responseHeaders, never()).add(eq(HttpHeaders.SET_COOKIE), anyString());
    }

    @Test
    public void testDevelopmentHandle() throws Exception {
        MultiMap headers = new CaseInsensitiveMultiMap();
        headers.add(SecurityEnforcer.X_REQUESTED_WITH, "XMLHttpRequest");
        headers.add(SecurityEnforcer.XSRF_TOKEN, NSA_PROOF_TOKEN);
        when(serverRequest.headers()).thenReturn(headers);
        when(serverRequest.path()).thenReturn(PATH);
        System.setProperty(StringConstants.DEV_MODE.getValue(), "true");
        enforcer.handle(serverRequest);
        verify(serverResponse, never()).write(SecurityEnforcer.SECURITY_TOKEN);
        verify(responseHeaders, never()).add(eq(HttpHeaders.SET_COOKIE), anyString());
    }

    @Test
    public void testTokenCreation() throws Exception {
        MultiMap headers = new CaseInsensitiveMultiMap();
        when(serverRequest.path()).thenReturn("/");
        when(serverResponse.headers()).thenReturn(headers);
        System.setProperty(StringConstants.DEV_MODE.getValue(), "true");
        enforcer.handle(serverRequest);
        verify(serverResponse, never()).write(SecurityEnforcer.SECURITY_TOKEN);
        assertTrue(headers.contains(HttpHeaders.SET_COOKIE));
        assertNotNull(headers.get(HttpHeaders.SET_COOKIE));
        Cookie cookie = new DefaultCookie(SecurityEnforcer.XSRF_TOKEN, NSA_PROOF_TOKEN);
        cookie.setDomain("localhost");
        cookie.setPath("/");
        assertEquals(ServerCookieEncoder.encode(cookie), headers.get(HttpHeaders.SET_COOKIE));
    }
}