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

package com.ejisto.modules.web;

import com.fasterxml.jackson.annotation.JsonCreator;

import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: celestino
 * Date: 6/27/12
 * Time: 7:01 PM
 */
public final class MockedFieldRequest {

    private static final MockedFieldRequest ALL_FIELDS = new MockedFieldRequest(null, null, null, false);

    private final String contextPath;
    private final String className;
    private final String fieldName;

    private MockedFieldRequest(String contextPath,
                               String className,
                               String fieldName,
                               boolean allClasses) {
        this.contextPath = contextPath;
        this.className = className;
        this.fieldName = fieldName;
    }

    public String getContextPath() {
        return contextPath;
    }

    public String getClassName() {
        return className;
    }

    public String getFieldName() {
        return fieldName;
    }

    public boolean areAllClassPropertiesRequested() {
        return fieldName == null;
    }

    public boolean areAllContextPathFieldsRequested() {
        return contextPath != null && fieldName == null && className == null;
    }

    public boolean areAllFieldsRequested() {
        return fieldName == null && contextPath == null && className == null;
    }

    public static MockedFieldRequest requestAllFields() {
        return ALL_FIELDS;
    }

    public static MockedFieldRequest requestAllFieldsOf(String contextPath, String className) {
        return new MockedFieldRequest(contextPath, className, null, false);
    }

    public static MockedFieldRequest requestAllClasses(String contextPath) {
        return new MockedFieldRequest(contextPath, null, null, true);
    }

    public static MockedFieldRequest requestSingleField(String contextPath, String className, String fieldName) {
        return new MockedFieldRequest(contextPath, className, fieldName, false);
    }

    @JsonCreator
    public static MockedFieldRequest deserialize(Map<String, Object> delegate) {
        String contextPath = (String) delegate.get("contextPath");
        String className = (String) delegate.get("className");
        if (contextPath == null && className == null) {
            return ALL_FIELDS;
        }
        return requestSingleField(contextPath, className, (String) delegate.get("fieldName"));
    }

}
