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

package com.ejisto.modules.recorder;

import javax.servlet.http.Cookie;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: celestino
 * Date: 8/28/12
 * Time: 5:32 PM
 */
public class DataCollector {

    private final Map<String, String> requestParameters;
    private final Map<String, Object> requestAttributes;
    private final List<String> requestDispatcherRedirection;
    private final List<String> permanentRedirections;
    private final Set<ResponseHeader> headers;
    private final List<Cookie> cookies;


    public DataCollector() {
        requestParameters = new TreeMap<>();
        requestAttributes = new TreeMap<>();
        requestDispatcherRedirection = new ArrayList<>();
        permanentRedirections = new ArrayList<>();
        headers = new TreeSet<>(ResponseHeader.COMPARATOR);
        cookies = new ArrayList<>();
    }

    public void putRequestParameter(String name, String value) {
        requestParameters.put(name, value);
    }

    public void putRequestAttribute(String name, Object value) {
        requestAttributes.put(name, value);
    }

    public void addResourcePath(String resourcePath) {
        requestDispatcherRedirection.add(resourcePath);
    }

    public void addPermanentRedirection(String path) {
        permanentRedirections.add(path);
    }

    public void addResponseHeader(ResponseHeader header) {
        headers.add(header);
    }

    public void addCookie(Cookie cookie) {
        cookies.add(cookie);
    }

    public CollectedData getResult() {
        return new CollectedData(requestParameters, requestAttributes, requestDispatcherRedirection,
                                 permanentRedirections, headers, cookies);
    }

}
