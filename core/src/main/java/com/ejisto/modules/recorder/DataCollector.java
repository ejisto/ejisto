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

import org.apache.commons.codec.digest.DigestUtils;

import javax.servlet.http.Cookie;
import java.util.*;

import static java.util.Collections.*;

/**
 * Created by IntelliJ IDEA.
 * User: celestino
 * Date: 8/28/12
 * Time: 5:32 PM
 */
public class DataCollector {

    private final CollectedData result;

    public DataCollector() {
        result = new CollectedData();
    }

    public void putRequestParameter(String name, String value) {
        result.requestParameters.put(name, value);
    }

    public void putRequestAttribute(String name, Object value) {
        result.requestAttributes.put(name, value);
    }

    public void addResourcePath(String resourcePath) {
        result.requestDispatcherRedirection.add(resourcePath);
    }

    public void addPermanentRedirection(String path) {
        result.permanentRedirections.add(path);
    }

    public void addResponseHeader(ResponseHeader header) {
        result.headers.add(header);
    }

    public void addCookie(Cookie cookie) {
        result.cookies.add(cookie);
    }

    public CollectedData getResult() {
        return result;
    }

    public final class CollectedData {

        private final Map<String, String> requestParameters;
        private final Map<String, Object> requestAttributes;
        private final List<String> requestDispatcherRedirection;
        private final List<String> permanentRedirections;
        private final Set<ResponseHeader> headers;
        private final List<Cookie> cookies;

        public CollectedData() {
            requestParameters = new TreeMap<>();
            requestAttributes = new TreeMap<>();
            requestDispatcherRedirection = new ArrayList<>();
            permanentRedirections = new ArrayList<>();
            headers = new TreeSet<>(ResponseHeader.COMPARATOR);
            cookies = new ArrayList<>();
        }

        public Map<String, String> getRequestParameters() {
            return unmodifiableMap(requestParameters);
        }

        public Map<String, Object> getRequestAttributes() {
            return unmodifiableMap(requestAttributes);
        }

        public List<String> getRequestDispatcherRedirection() {
            return unmodifiableList(requestDispatcherRedirection);
        }

        public List<String> getPermanentRedirections() {
            return unmodifiableList(permanentRedirections);
        }

        public Set<ResponseHeader> getHeaders() {
            return unmodifiableSet(headers);
        }

        public List<Cookie> getCookies() {
            return unmodifiableList(cookies);
        }

        public String getFullKey() {
            return buildKey(true);
        }

        public String getSmallKey() {
            return buildKey(false);
        }

        private String buildKey(boolean full) {
            StringBuilder clearText = new StringBuilder();
            for (String key : requestParameters.keySet()) {
                clearText.append(key);
                if(full) {
                    clearText.append("=").append(requestParameters.get(key));
                }
                clearText.append(";");
            }
            if (clearText.length() > 0) {
                clearText.deleteCharAt(clearText.length() - 1);
                return DigestUtils.sha256Hex(clearText.toString());
            }
            return null;
        }
    }

}
