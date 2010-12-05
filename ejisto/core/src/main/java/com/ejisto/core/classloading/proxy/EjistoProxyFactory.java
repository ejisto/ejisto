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

import com.ejisto.modules.dao.entities.MockedField;
import com.ejisto.modules.repository.MockedFieldsRepository;
import net.sf.cglib.proxy.Enhancer;
import ognl.ObjectNullHandler;

import javax.annotation.Resource;
import java.util.Map;

public class EjistoProxyFactory extends ObjectNullHandler {
    @Resource
    private MockedFieldsRepository mockedFieldsRepository;

    @SuppressWarnings("unchecked")
    public <T> T proxyClass(Class<T> target, String contextPath) {
        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(target);
        enhancer.setCallback(new EjistoProxy(mockedFieldsRepository.load(contextPath, target.getName())));
        return (T) enhancer.create();
    }

    public <T> T proxyClass(Class<T> target, MockedField mockedField) {
        return proxyClass(target, mockedField.getContextPath());
    }

    @Override
    public Object nullPropertyValue(Map context, Object target, Object property) {
        return super.nullPropertyValue(context, target, property);
    }
}
