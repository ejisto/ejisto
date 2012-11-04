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

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.codec.digest.DigestUtils;

import javax.servlet.http.Cookie;
import java.util.*;

import static java.util.Collections.*;

/**
 * Created by IntelliJ IDEA.
 * User: celestino
 * Date: 10/23/12
 * Time: 8:11 AM
 */
public final class CollectedData {

    private final Map<String, String> requestParameters;
    private final Map<String, Object> requestAttributes;
    private final List<String> requestDispatcherRedirection;
    private final List<String> permanentRedirections;
    private final Set<ResponseHeader> headers;
    private final List<Cookie> cookies;

    @JsonCreator
    public CollectedData(Map<String, String> requestParameters, Map<String, Object> requestAttributes,
                         List<String> requestDispatcherRedirection, List<String> permanentRedirections,
                         Set<ResponseHeader> headers, List<Cookie> cookies) {
        this.requestParameters = new TreeMap<>(requestParameters);
        this.requestAttributes = new TreeMap<>(requestAttributes);
        this.requestDispatcherRedirection = new ArrayList<>(requestDispatcherRedirection);
        this.permanentRedirections = new ArrayList<>(permanentRedirections);
        this.headers = new TreeSet<>(ResponseHeader.COMPARATOR);
        this.headers.addAll(headers);
        this.cookies = new ArrayList<>(cookies);
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
            if (full) {
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
