/*
 * Ejisto, a powerful developer assistant
 *
 * Copyright (C) 2010-2014 Celestino Bellone
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

package com.ejisto.modules.factory.impl;

import com.ejisto.core.classloading.proxy.EjistoProxyFactory;
import com.ejisto.modules.dao.entities.MockedField;
import com.ejisto.modules.repository.MockedFieldsRepository;

/**
 * Created by IntelliJ IDEA.
 * User: celestino
 * Date: 7/23/14
 * Time: 5:57 PM
 */
public class FakeDefaultObjectFactory extends DefaultObjectFactory {

    private final MockedFieldsRepository mockedFieldsRepository;

    public FakeDefaultObjectFactory(MockedFieldsRepository mockedFieldsRepository) {
        this.mockedFieldsRepository = mockedFieldsRepository;
    }

    @Override
    public Object create(MockedField m, Object actualValue) {
        try {
            Class<?> target = Class.forName(m.getClassName());
            return EjistoProxyFactory.proxyClass(target, m.getContextPath(), mockedFieldsRepository);
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException(e);
        }
    }
}
