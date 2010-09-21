/*
 * Ejisto, a powerful developer assistant
 *
 * Copyright (C) 2010  Celestino Bellone
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

public class MockedField {
    private long id;
    private String contextPath;
	private String className;
    private String fieldName;
    private String fieldType;
    private String fieldValue;
    
    public long getId() {
        return id;
    }
    
    public void setId(long id) {
        this.id = id;
    }
    
    public String getContextPath() {
		return contextPath;
	}

	public void setContextPath(String contextPath) {
		this.contextPath = contextPath;
	}

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public String getFieldType() {
        return fieldType;
    }

    public void setFieldType(String fieldType) {
        this.fieldType = fieldType;
    }

    public String getFieldValue() {
        return fieldValue;
    }

    public void setFieldValue(String fieldValue) {
        this.fieldValue = fieldValue;
    }

	@Override
	public String toString() {
		return "MockedField [id="+id+", contextPath=" + contextPath + ", className="
				+ className + ", fieldName=" + fieldName + ", fieldType="
				+ fieldType + ", fieldValue=" + fieldValue + "]";
	}
	
	public String getComparisonKey() {
	    return contextPath+"/"+className+"/"+fieldName;
	}
	
	public String getGroupKey() {
		return className.substring(0, className.lastIndexOf('.'));
	}
	
	public String getClassSimpleName() {
		return className.substring(className.lastIndexOf('.') + 1);
	}
	
}
