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

import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import static java.util.Arrays.asList;
import static org.apache.commons.lang3.StringUtils.join;

@Data
public class MockedFieldImpl implements MockedField {
    private long id;
    private String contextPath;
    private String className;
    private String fieldName;
    private String fieldType;
    private String fieldValue;
    private String expression;
    private String fieldElementType;
    private boolean active;
    private String link;
    private int recordedObjectHashCode;

    @Override
    public boolean isSimpleValue() {
        return expression == null;
    }

    @Override
    public String getComparisonKey() {
        return join(asList(contextPath, getPackageName(), className, fieldName), PATH_SEPARATOR);
    }

    @Override
    public String[] getParentClassPath() {
        String[] pathToClass = getClassName().split("[.\\$]");
        String[] path = new String[pathToClass.length + 1];
        path[0] = getContextPath();
        System.arraycopy(pathToClass, 0, path, 1, pathToClass.length);
        return path;
    }

    @Override
    public String[] getPath() {
        String[] pathToClass = getClassName().split("[.\\$]");
        String[] path = new String[pathToClass.length + 2];
        path[0] = getContextPath();
        path[path.length - 1] = getFieldName();
        System.arraycopy(pathToClass, 0, path, 1, pathToClass.length);
        return path;
    }

    @Override
    public String getParentClassPathAsString() {
        return join(getParentClassPath(), PATH_SEPARATOR);
    }

    @Override
    public String getPackageName() {
        if (className.lastIndexOf('.') > -1) {
            return className.substring(0, className.lastIndexOf('.'));
        }
        return "0";
    }

    @Override
    public String getClassSimpleName() {
        return className.substring(className.lastIndexOf('.') + 1);
    }

    @Override
    public String getFieldElementType() {
        return fieldElementType;
    }

    @Override
    public void setFieldElementType(String fieldElementType) {
        this.fieldElementType = fieldElementType;
    }

    @Override
    public String getCompleteDescription() {
        return toString();
    }

    @Override
    public String getCompleteFieldType() {
        return getFieldType();
    }

    @Override
    public void setLink(String link) {
        this.link = link;
    }

    @Override
    public String getLink() {
        return link;
    }

    @Override
    public boolean isLinked() {
        return StringUtils.isNotBlank(link);
    }

    @Override
    public int getRecordedObjectHashCode() {
        return recordedObjectHashCode;
    }

    @Override
    public void setRecordedObjectHashCode(int recordedObjectHashCode) {
        this.recordedObjectHashCode = recordedObjectHashCode;
    }

    @Override
    public MockedField unwrap() {
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof MockedFieldImpl)) {
            return false;
        }
        if (this == o) {
            return true;
        }
        MockedFieldImpl that = (MockedFieldImpl) o;
        return new EqualsBuilder()
                .append(id, that.id)
                .append(className, that.className)
                .append(contextPath, that.contextPath)
                .append(link, that.link)
                .append(fieldElementType, that.fieldElementType)
                .append(fieldName, that.fieldName)
                .append(fieldType, that.fieldType)
                .append(recordedObjectHashCode, that.recordedObjectHashCode)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(13, 37)
                .append(id)
                .append(className)
                .append(contextPath)
                .append(link)
                .append(fieldElementType)
                .append(fieldName)
                .append(fieldType)
                .append(recordedObjectHashCode)
                .build();
    }

    @Override
    public int compareTo(MockedField o) {
        if (id == o.getId()) {
            return 0;
        }
        return getComparisonKey().compareTo(o.getComparisonKey());
    }

    public static MockedFieldImpl copyOf(MockedFieldImpl source) {
        MockedFieldImpl copy = new MockedFieldImpl();
        copy.active = source.active;
        copy.contextPath = source.contextPath;
        copy.className = source.className;
        copy.fieldName = source.fieldName;
        copy.fieldType = source.fieldType;
        copy.fieldValue = source.fieldValue;
        copy.expression = source.expression;
        copy.fieldElementType = source.fieldElementType;
        copy.link = source.link;
        copy.recordedObjectHashCode = source.recordedObjectHashCode;
        return copy;
    }

}
