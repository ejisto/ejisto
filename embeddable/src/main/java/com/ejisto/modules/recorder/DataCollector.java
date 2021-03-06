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
import lombok.extern.java.Log;

import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.logging.Level;

import static com.ejisto.constants.StringConstants.REQUEST_ATTRIBUTE;
import static com.ejisto.constants.StringConstants.SESSION_ATTRIBUTE;
import static com.ejisto.modules.web.util.FieldSerializationUtil.translateObject;

/**
 * Created by IntelliJ IDEA.
 * User: celestino
 * Date: 8/28/12
 * Time: 5:32 PM
 */
@Log
public class DataCollector {

    private final String requestURI;
    private final Map<String, List<String>> requestParameters;
    private final Map<String, Object> requestAttributes;
    private final Map<String, Object> sessionAttributes;
    private final List<String> requestDispatcherRedirection;
    private final List<String> permanentRedirection;
    private final Set<ResponseHeader> headers;
    private final String contextPath;

    public DataCollector(HttpServletRequest request) {
        requestURI = extractRequestURI(request);
        requestParameters = new TreeMap<String, List<String>>();
        requestAttributes = new TreeMap<String, Object>();
        sessionAttributes = new TreeMap<String, Object>();
        requestDispatcherRedirection = new ArrayList<String>();
        permanentRedirection = new ArrayList<String>();
        headers = new TreeSet<ResponseHeader>(ResponseHeader.COMPARATOR);
        this.contextPath = request.getContextPath();
    }

    public void putRequestParameter(String name, String value) {
        log.log(Level.INFO, "putting requestParameter {0}", name);
        if (!requestParameters.containsKey(name)) {
            requestParameters.put(name, new ArrayList<String>());
        }
        requestParameters.get(name).add(value);
    }

    public void putRequestAttribute(String name, Object value) {
        log.log(Level.INFO, "putting requestAttribute {0}", name);
        requestAttributes.put(name, value);
    }

    public void putSessionAttribute(String name, Object value) {
        log.log(Level.INFO, "putting sessionAttribute {0}", name);
        sessionAttributes.put(name, value);
    }

    public void addResourcePath(String resourcePath) {
        log.log(Level.INFO, "adding ResourcePath {0}", resourcePath);
        requestDispatcherRedirection.add(resourcePath);
    }

    public void addPermanentRedirection(String path) {
        log.log(Level.INFO, "adding permanent redirection {0}", path);
        permanentRedirection.add(path);
    }

    public void addResponseHeader(ResponseHeader header) {
        headers.add(header);
    }

    public CollectedData getResult() {
        return new CollectedData(requestURI, translateRequestParameters(requestParameters),
                                 translateRequestAttributes(requestAttributes, contextPath),
                                 translateSessionAttributes(sessionAttributes, contextPath),
                                 requestDispatcherRedirection,
                                 permanentRedirection, headers, contextPath, false);
    }

    private static Map<String, String[]> translateRequestParameters(Map<String, List<String>> requestParameters) {
        Map<String, String[]> result = new TreeMap<String, String[]>();
        for (Map.Entry<String, List<String>> entry : requestParameters.entrySet()) {
            List<String> value = entry.getValue();
            result.put(entry.getKey(), entry.getValue().toArray(new String[value.size()]));
        }
        return result;
    }

    private static Map<String, List<MockedField>> translateRequestAttributes(Map<String, Object> requestAttributes, String contextPath) {
        return translateAttributes(requestAttributes, contextPath, REQUEST_ATTRIBUTE.getValue());
    }

    private static Map<String, List<MockedField>> translateSessionAttributes(Map<String, Object> requestAttributes, String contextPath) {
        return translateAttributes(requestAttributes, contextPath, SESSION_ATTRIBUTE.getValue());
    }

    private static Map<String, List<MockedField>> translateAttributes(Map<String, Object> requestAttributes, String contextPath, String key) {
        Map<String, List<MockedField>> out = new HashMap<String, List<MockedField>>(requestAttributes.size());
        for (Map.Entry<String, Object> entry : requestAttributes.entrySet()) {
            out.put(entry.getKey(),
                    translateObject(entry.getValue(), key, entry.getKey(), contextPath));
        }
        return out;
    }

    private static String extractRequestURI(HttpServletRequest request) {
        return request.getRequestURI();
    }
}
