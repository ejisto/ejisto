/*
 * Ejisto, a powerful developer assistant
 *
 * Copyright (C) 2010-2012  Celestino Bellone
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

import com.ejisto.core.ApplicationException;
import com.ejisto.modules.dao.entities.MockedField;
import com.ejisto.modules.dao.entities.MockedFieldImpl;
import com.ejisto.modules.factory.ObjectFactory;

import java.lang.reflect.Array;

/**
 * Created by IntelliJ IDEA.
 * User: celestino
 * Date: 3/14/12
 * Time: 6:57 PM
 */
public class ArrayFactory<T> implements ObjectFactory<T[]> {

    private ObjectFactory<T> elementFactory;

    public ArrayFactory(ObjectFactory<T> elementFactory) {
        this.elementFactory = elementFactory;
    }

    @Override
    public String getTargetClassName() {
        return String.format("Array [L%s;", elementFactory.getTargetClassName());
    }

    @Override
    public T[] create(MockedField m, T[] actualValue) {
        return createArray(elementFactory, m, actualValue);
    }

    @Override
    public boolean supportsRandomValuesCreation() {
        return false;
    }

    @Override
    public T[] createRandomValue() {
        return null;
    }

    private <T> T[] createArray(ObjectFactory<T> factory, MockedField m, T[] actualValue) {
        String[] values = m.getFieldValue().split("[,;]");
        T[] array = createArray(factory.getTargetClassName(), values.length);
        boolean equalSize = actualValue != null && actualValue.length == values.length;
        for (int i = 0; i < values.length; i++) {
            MockedField temp = new MockedFieldImpl();
            temp.copyFrom(m);
            temp.setFieldValue(values[i]);
            array[i] = factory.create(temp, equalSize ? actualValue[i] : null);
        }
        return array;
    }

    @SuppressWarnings("unchecked")
    private <T> T[] createArray(String elementClassName, int length) {
        try {
            Class<T> clazz = (Class<T>) Class.forName(elementClassName);
            return (T[]) Array.newInstance(clazz, length);
        } catch (ClassNotFoundException e) {
            throw new ApplicationException(e);
        }
    }
}
