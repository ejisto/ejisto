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

package com.ejisto.core.classloading.proxy;

import com.ejisto.core.classloading.javassist.EjistoMethodHandler;
import com.ejisto.modules.dao.entities.MockedField;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

import java.lang.reflect.Method;
import java.util.List;

import static ch.lambdaj.Lambda.*;
import static com.ejisto.core.classloading.util.ReflectionUtils.*;
import static org.hamcrest.Matchers.equalTo;

public class EjistoProxy implements MethodInterceptor {
    private List<MockedField> mockedFields;
    private EjistoMethodHandler methodHandler;

    public EjistoProxy(List<MockedField> mockedFields) {
        this.mockedFields = mockedFields;
        this.methodHandler = new EjistoMethodHandler(mockedFields);
    }
    
    @Override
    public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable {
        String name = method.getName();
        if(isGetter(name)) return handleGetter(obj, method, args, proxy);
        else if(isSetter(name)) return handleSetter(obj, method, args, proxy);
        return proxy.invokeSuper(obj, args);
    }

    private Object handleGetter(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable {
        MockedField mockedField = getMockedFieldFor(method.getName());
        if(mockedField == null) return proxy.invokeSuper(obj, args);
        return methodHandler.invoke(obj, method, null, args);
    }

    private Object handleSetter(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable {
        return proxy.invokeSuper(obj, args);
    }

    private MockedField getMockedFieldFor(String methodName) {
        return selectFirst(mockedFields, having(on(MockedField.class).getFieldName(), equalTo(getFieldName(methodName))));
    }

}
