/*
 * Ejisto, a powerful developer assistant
 *
 * Copyright (C) 2010-2013 Celestino Bellone
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

import com.ejisto.modules.dao.entities.MockedField;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.*;

import static com.ejisto.modules.web.util.DigestUtil.sha256Digest;
import static java.util.Collections.*;

/**
 * Created by IntelliJ IDEA.
 * User: celestino
 * Date: 10/23/12
 * Time: 8:11 AM
 */
public final class CollectedData {

    private final Map<String, String> requestParameters;
    private final Map<String, List<MockedField>> requestAttributes;
    private final Map<String, List<MockedField>> sessionAttributes;
    private final List<String> requestDispatcherRedirection;
    private final List<String> permanentRedirections;
    private final Set<ResponseHeader> headers;
    private final String contextPath;

    @JsonCreator
    public CollectedData(@JsonProperty("requestParameters") Map<String, String> requestParameters,
                         @JsonProperty("requestAttributes") Map<String, List<MockedField>> requestAttributes,
                         @JsonProperty("sessionAttributes") Map<String, List<MockedField>> sessionAttributes,
                         @JsonProperty("requestDispatcherRedirection") List<String> requestDispatcherRedirection,
                         @JsonProperty("permanentRedirections") List<String> permanentRedirections,
                         @JsonProperty("headers") Set<ResponseHeader> headers,
                         @JsonProperty("contextPath") String contextPath) {
        this.requestParameters = new TreeMap<String, String>(requestParameters);
        this.requestAttributes = new TreeMap<String, List<MockedField>>(requestAttributes);
        this.sessionAttributes = new TreeMap<String, List<MockedField>>(sessionAttributes);
        this.requestDispatcherRedirection = new ArrayList<String>(requestDispatcherRedirection);
        this.permanentRedirections = new ArrayList<String>(permanentRedirections);
        this.contextPath = contextPath;
        this.headers = new TreeSet<ResponseHeader>(ResponseHeader.COMPARATOR);
        this.headers.addAll(headers);
    }

    public Map<String, String> getRequestParameters() {
        return unmodifiableMap(requestParameters);
    }

    public Map<String, List<MockedField>> getRequestAttributes() {
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

    public Map<String, List<MockedField>> getSessionAttributes() {
        return sessionAttributes;
    }

    public String getContextPath() {
        return contextPath;
    }

    @JsonIgnore
    public String getFullKey() {
        return buildKey(true);
    }

    @JsonIgnore
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
            return sha256Digest(clearText.toString());
        }
        return null;
    }
}
