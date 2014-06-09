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

package com.ejisto.modules.dao.db.util;

import com.ejisto.modules.dao.entities.MockedField;
import com.ejisto.modules.dao.entities.MockedFieldImpl;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

/**
 * Created by IntelliJ IDEA.
 * User: celestino
 * Date: 3/5/13
 * Time: 10:03 PM
 */
public final class MockedFieldContainer implements Comparable<MockedFieldContainer> {

    private final String className;
    private final String fieldName;
    private final MockedFieldImpl mockedField;

    @JsonCreator
    MockedFieldContainer(@JsonProperty("className") String className, @JsonProperty(
            "fieldName") String fieldName, @JsonProperty("mockedField") MockedFieldImpl mockedField) {
        this.className = className;
        this.fieldName = fieldName;
        this.mockedField = mockedField;
    }

    public String getClassName() {
        return className;
    }

    public String getFieldName() {
        return fieldName;
    }

    public MockedField getMockedField() {
        return mockedField;
    }

    public static MockedFieldContainer from(MockedField mockedField) {
        Objects.requireNonNull(mockedField, "Source MockedField cannot be null");
        return new MockedFieldContainer(mockedField.getClassName(), mockedField.getFieldName(),
                                        MockedFieldImpl.copyOf((MockedFieldImpl) mockedField.unwrap()));
    }

    @Override
    @SuppressWarnings("NullableProblems")
    public int compareTo(MockedFieldContainer other) {
        int result = getClassName().compareTo(other.getClassName());
        if (result != 0) {
            return result;
        }
        return getFieldName().compareTo(other.getFieldName());
    }

    public boolean wraps(MockedField field) {
        return mockedField.getContextPath().equals(field.getContextPath())
                && mockedField.getClassName().equals(field.getClassName())
                && mockedField.getFieldName().equals(field.getFieldName());

    }
}
