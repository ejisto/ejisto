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

package com.ejisto.core.classloading.javassist;

import com.ejisto.modules.dao.entities.MockedField;
import javassist.util.proxy.MethodHandler;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Objects;

import static ch.lambdaj.Lambda.*;
import static com.ejisto.core.classloading.util.ReflectionUtils.getFieldName;
import static com.ejisto.core.classloading.util.ReflectionUtils.isGetterForProperty;
import static org.hamcrest.Matchers.equalTo;

public class EjistoMethodHandler implements MethodHandler {

    private Collection<MockedField> fields;

    public EjistoMethodHandler(Collection<MockedField> fields) {
        this.fields = fields;
    }

    @Override
    public Object invoke(Object self, Method thisMethod, Method proceed, Object[] args) throws Throwable {
        return getFieldValue(thisMethod);
    }

    private Object getFieldValue(Method method) throws Exception {
        MockedField mockedField = retrieveFieldToMock(method.getName());
        Objects.requireNonNull(mockedField);
        if (!mockedField.isActive()) {
            return null;
        }
        if (!isGetterForProperty(method.getName(), mockedField.getFieldName())) {
            throw new IllegalArgumentException("error: undefined method [" + method.getName() + "]");
        }

        Class<?> returnType = method.getReturnType();
        Constructor<?> constructor = returnType.getConstructor(String.class);
        return constructor.newInstance(mockedField.getFieldValue());
    }

    private MockedField retrieveFieldToMock(String methodName) {
        return selectFirst(fields, having(on(MockedField.class).getFieldName(), equalTo(getFieldName(methodName))));
    }

}
