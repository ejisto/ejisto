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

package com.ejisto.core.classloading.javassist;

import com.ejisto.modules.dao.entities.MockedField;
import javassist.util.proxy.MethodFilter;

import java.lang.reflect.Method;
import java.util.Collection;

import static ch.lambdaj.Lambda.*;
import static com.ejisto.core.classloading.util.ReflectionUtils.getFieldName;
import static com.ejisto.core.classloading.util.ReflectionUtils.isGetter;
import static org.hamcrest.Matchers.equalTo;

public class EjistoMethodFilter implements MethodFilter {
	
	private Collection<MockedField> fields;
    private String contextPath;
    
	public EjistoMethodFilter(String contextPath, Collection<MockedField> fields) {
	    this.contextPath = contextPath;
		this.fields=fields;
	}

	@Override
	public boolean isHandled(Method m) {
		String methodName = m.getName();
		return isGetter(methodName) && isFieldHandled(getFieldName(methodName));
	}
	
	public boolean isFieldHandled(String fieldName) {
		return selectFirst(fields, having(on(MockedField.class).getFieldName(),equalTo(fieldName))) != null;
	}
	
	public String getContextPath() {
        return contextPath;
    }

}
