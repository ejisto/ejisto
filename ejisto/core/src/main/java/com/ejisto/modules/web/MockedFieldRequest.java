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

package com.ejisto.modules.web;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by IntelliJ IDEA.
 * User: celestino
 * Date: 6/27/12
 * Time: 7:01 PM
 */
public final class MockedFieldRequest {
    private final String contextPath;
    private final String className;
    private final String fieldName;

    @JsonCreator
    public MockedFieldRequest(@JsonProperty("contextPath") String contextPath,
                              @JsonProperty("className") String className,
                              @JsonProperty("fieldName") String fieldName) {
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

}
