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
import com.ejisto.modules.repository.MockedFieldsRepository;
import net.sf.cglib.proxy.Enhancer;
import ognl.ObjectNullHandler;

import java.util.Map;

public final class EjistoProxyFactory extends ObjectNullHandler {
    private static final EjistoProxyFactory INSTANCE = new EjistoProxyFactory();
    private MockedFieldsRepository mockedFieldsRepository;

    public static EjistoProxyFactory getInstance() {
        return INSTANCE;
    }

    private EjistoProxyFactory() {
        this.mockedFieldsRepository = MockedFieldsRepository.getInstance();
    }

    @SuppressWarnings("unchecked")
    public <T> T proxyClass(Class<T> target, String contextPath) {
        init();
        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(target);
        enhancer.setCallback(new EjistoProxy(mockedFieldsRepository.load(contextPath, target.getName())));
        return (T) enhancer.create();
    }

    @SuppressWarnings("unchecked")
    public <T> T proxyClass(String targetClassName, String contextPath) throws ClassNotFoundException {
        return (T) proxyClass(Class.forName(targetClassName), contextPath);
    }

    public <T> T proxyClass(Class<T> target, MockedField mockedField) {
        return proxyClass(target, mockedField.getContextPath());
    }

    @Override
    public Object nullPropertyValue(Map context, Object target, Object property) {
        return super.nullPropertyValue(context, target, property);
    }

    private synchronized void init() {
        if (mockedFieldsRepository == null) {
            mockedFieldsRepository = MockedFieldsRepository.getInstance();
        }
    }
}
