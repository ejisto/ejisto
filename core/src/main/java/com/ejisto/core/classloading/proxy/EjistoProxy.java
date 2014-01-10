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

package com.ejisto.core.classloading.proxy;

import com.ejisto.modules.dao.entities.MockedField;
import com.ejisto.modules.factory.ObjectFactory;
import com.ejisto.modules.repository.ObjectFactoryRepository;
import com.ejisto.util.LambdaUtil;
import lombok.extern.log4j.Log4j;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;

import static com.ejisto.core.classloading.util.ReflectionUtils.*;

@Log4j
public class EjistoProxy implements MethodInterceptor {
    private final List<MockedField> mockedFields;
    private final ObjectFactoryRepository objectFactoryRepository;

    public EjistoProxy(List<MockedField> mockedFields) {
        this.mockedFields = mockedFields;
        this.objectFactoryRepository = new ObjectFactoryRepository(null, null);
    }

    @Override
    public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable {
        String name = method.getName();
        if (isGetter(name)) {
            return handleGetter(obj, method, args, proxy);
        } else if (isSetter(name)) {
            return handleSetter(obj, method, args, proxy);
        }
        return proxy.invokeSuper(obj, args);
    }

    private <T> T handleGetter(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable {
        Optional<MockedField> optional = getMockedFieldFor(method.getName());
        @SuppressWarnings("unchecked")
        T actualValue = (T)proxy.invokeSuper(obj, args);
        if (!optional.isPresent()) {
            return actualValue;
        }
        log.trace("mockedField is not null. Handling getter.");
        MockedField mockedField = optional.get();
        ObjectFactory<T> factory = objectFactoryRepository.getObjectFactory(
                method.getReturnType().getName(),
                mockedField.getContextPath());
        log.trace(String.format("got %s for %s", factory, mockedField));
        return factory.create(mockedField, actualValue);
    }

    private Object handleSetter(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable {
        return proxy.invokeSuper(obj, args);
    }

    private Optional<MockedField> getMockedFieldFor(String methodName) {
        return mockedFields.stream().filter(LambdaUtil.findFieldByName(getFieldName(methodName))).findFirst();
    }

}
