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

package com.ejisto.modules.dao.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import java.io.Serializable;

/**
 * Created by IntelliJ IDEA.
 * User: celestino
 * Date: Dec 5, 2010
 * Time: 10:39:18 AM
 */
@JsonDeserialize(as = MockedFieldImpl.class)
public interface MockedField extends ComplexValuesAware, Comparable<MockedField>, Serializable, Entity<String> {

    String PATH_SEPARATOR = ">";

    long getId();

    void setId(long id);

    String getContextPath();

    void setContextPath(String contextPath);

    String getClassName();

    void setClassName(String className);

    String getFieldName();

    void setFieldName(String fieldName);

    String getFieldType();

    void setFieldType(String fieldType);

    String getFieldValue();

    void setFieldValue(String fieldValue);

    boolean isActive();

    void setActive(boolean active);

    String getExpression();

    void setExpression(String expression);

    String getFieldElementType();

    void setFieldElementType(String fieldElementType);

    @JsonIgnore
    String getComparisonKey();

    @JsonIgnore
    String[] getParentClassPath();

    @JsonIgnore
    String[] getPath();

    @JsonIgnore
    String getParentClassPathAsString();

    @JsonIgnore
    String getPackageName();

    @JsonIgnore
    String getClassSimpleName();

    void setLink(String link);

    String getLink();

    @JsonIgnore
    boolean isLinked();

    int getRecordedObjectHashCode();

    void setRecordedObjectHashCode(int recordedObjectHashCode);

}
