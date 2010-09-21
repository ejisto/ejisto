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
import javassist.util.proxy.MethodHandler;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Collection;

import static ch.lambdaj.Lambda.*;
import static org.hamcrest.Matchers.equalTo;

public class EjistoMethodHandler implements MethodHandler {
	
	private Collection<MockedField> fields;

	public EjistoMethodHandler(Collection<MockedField> fields) {
		this.fields=fields;
	}

	@Override
	public Object invoke(Object self, Method thisMethod, Method proceed,
			Object[] args) throws Throwable {
		return getFieldValue(thisMethod);
	}
	
	private Object getFieldValue(Method method) throws Exception {
		MockedField mockedField = retrieveFieldToMock(method.getName());
		Assert.notNull(mockedField);
		String storedMethod = "get"+StringUtils.capitalize(mockedField.getFieldName());
		Assert.isTrue(method.getName().equals(storedMethod), "method mismatch: declared method ["+method.getName()+"], stored method ["+storedMethod+"]");
		Class<?> returnType = method.getReturnType();
		Constructor<?> constructor = returnType.getConstructor(String.class);
		return constructor.newInstance(mockedField.getFieldValue());
	}
	
	private MockedField retrieveFieldToMock(String methodName) {
		return selectFirst(fields, having(on(MockedField.class).getFieldName(), equalTo(StringUtils.uncapitalize(methodName.substring(4)))));
	}

}
