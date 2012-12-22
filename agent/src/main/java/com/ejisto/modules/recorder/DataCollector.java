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

import com.ejisto.modules.dao.entities.MockedField;
import lombok.extern.java.Log;

import java.util.*;

import static com.ejisto.constants.StringConstants.REQUEST_ATTRIBUTE;
import static com.ejisto.modules.web.util.FieldSerializationUtil.translateObject;

/**
 * Created by IntelliJ IDEA.
 * User: celestino
 * Date: 8/28/12
 * Time: 5:32 PM
 */
@Log
public class DataCollector {

    private final Map<String, String> requestParameters;
    private final Map<String, Object> requestAttributes;
    private final Map<String, Object> sessionAttributes;
    private final List<String> requestDispatcherRedirection;
    private final List<String> permanentRedirections;
    private final Set<ResponseHeader> headers;
    private final String contextPath;

    public DataCollector(String contextPath) {
        requestParameters = new TreeMap<String, String>();
        requestAttributes = new TreeMap<String, Object>();
        sessionAttributes = new TreeMap<String, Object>();
        requestDispatcherRedirection = new ArrayList<String>();
        permanentRedirections = new ArrayList<String>();
        headers = new TreeSet<ResponseHeader>(ResponseHeader.COMPARATOR);
        this.contextPath = contextPath;
    }

    public void putRequestParameter(String name, String value) {
        requestParameters.put(name, value);
    }

    public void putRequestAttribute(String name, Object value) {
        requestAttributes.put(name, value);
    }

    public void putSessionAttribute(String name, Object value) {
        sessionAttributes.put(name, value);
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

    public CollectedData getResult() {
        return new CollectedData(requestParameters, translateAttributes(requestAttributes, contextPath),
                                 translateAttributes(sessionAttributes, contextPath),
                                 requestDispatcherRedirection,
                                 permanentRedirections, headers, contextPath);
    }

    private static Map<String, List<MockedField>> translateAttributes(Map<String, Object> requestAttributes, String contextPath) {
        Map<String, List<MockedField>> out = new HashMap<String, List<MockedField>>(requestAttributes.size());
        for (Map.Entry<String, Object> entry : requestAttributes.entrySet()) {
            out.put(entry.getKey(),
                    translateObject(entry.getValue(), REQUEST_ATTRIBUTE.getValue(), entry.getKey(), contextPath));
        }
        return out;
    }


}
