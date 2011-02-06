/*
 * Ejisto, a powerful developer assistant
 *
 * Copyright (C) 2011  Celestino Bellone
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


    @Override
    public long getId() {
        return id;
    }

    @Override
    public void setId(long id) {
        this.id = id;
    }

    @Override
    public String getContextPath() {
        return contextPath;
    }

    @Override
    public void setContextPath(String contextPath) {
        this.contextPath = contextPath;
    }

    @Override
    public String getClassName() {
        return className;
    }

    @Override
    public void setClassName(String className) {
        this.className = className;
    }

    @Override
    public String getFieldName() {
        return fieldName;
    }

    @Override
    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    @Override
    public String getFieldType() {
        return fieldType;
    }

    @Override
    public void setFieldType(String fieldType) {
        this.fieldType = fieldType;
    }

    @Override
    public String getFieldValue() {
        return fieldValue;
    }

    @Override
    public void setFieldValue(String fieldValue) {
        this.fieldValue = fieldValue;
    }

    @Override
    public boolean isActive() {
        return active;
    }

    @Override
    public void setActive(boolean active) {
        this.active = active;
    }

    @Override
    public boolean isSimpleValue() {
        return expression == null;
    }

    @Override
    public String getExpression() {
        return expression;
    }

    @Override
    public void setExpression(String expression) {
        this.expression = expression;
    }

    @Override
    public String toString() {
        return new StringBuilder("MockedField [id=").append(id).append(", contextPath=").append(contextPath).append(", className=")
                .append(className).append(", fieldName=").append(fieldName).append(", fieldType=")
                .append(fieldType).append(", fieldValue=").append(fieldValue).append(", active=").append(active).append("]").toString();
    }

    @Override
    public String getComparisonKey() {
        return contextPath + "/" + className + "/" + fieldName;
    }

    @Override
    public String getPackageName() {
        return className.substring(0, className.lastIndexOf('.'));
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

}
