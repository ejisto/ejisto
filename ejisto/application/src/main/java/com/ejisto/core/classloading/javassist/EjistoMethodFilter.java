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
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;
import java.util.Collection;

import static ch.lambdaj.Lambda.*;
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
		return methodName.startsWith("get") && isFieldHandled(methodName.substring(4));
	}
	
	public boolean isFieldHandled(String fieldName) {
		return selectFirst(fields, having(on(MockedField.class).getFieldName(),equalTo(StringUtils.uncapitalize(fieldName)))) != null;
	}
	
	public String getContextPath() {
        return contextPath;
    }

}
