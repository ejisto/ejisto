/*
 * Ejisto, a powerful developer assistant
 *
 * Copyright (C) 2010-2011  Celestino Bellone
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

import com.ejisto.modules.dao.entities.MockedField;
import com.ejisto.modules.factory.AbstractContainerFactory;
import com.ejisto.modules.factory.ObjectFactory;
import ognl.Ognl;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by IntelliJ IDEA.
 * User: celestino
 * Date: 1/15/11
 * Time: 7:41 PM
 */
public class CollectionFactory<Y> extends AbstractContainerFactory<Collection<Y>, Y> {
    @Override
    public String getTargetClassName() {
        return "java.util.Collection";
    }

    @Override
    public Collection<Y> create(MockedField m, Collection<Y> actualValue) {
        ObjectFactory<Y> elementObjectFactory = loadElementObjectFactory(m.getFieldElementType(), m.getContextPath());
        Collection<Y> value = new ArrayList<Y>();
        applyExpressions(value, m.getExpression(), elementObjectFactory, m, null);
        return value;
    }

    private void applyExpressions(Collection<Y> in, String expression, ObjectFactory<Y> elementObjectFactory, MockedField mockedField, Y actualValue) {
        try {
            int size = 10;
            if(expression != null) {
                String[] expressions = expression.split(";");
                String[] keyValue;
                for (String exp : expressions) {
                    keyValue = exp.split("=");
                    if (keyValue[0].equals("size"))
                        size = Integer.parseInt(keyValue[1]);
                    else
                        Ognl.setValue(keyValue[0], in, keyValue[1]);
                }
            }
            fillCollection(in, size, elementObjectFactory, mockedField, actualValue);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void fillCollection(Collection<Y> in, int size, ObjectFactory<Y> elementObjectFactory, MockedField mockedField, Y actualValue) {
        for (int i = 0; i < size; i++) {
            in.add(elementObjectFactory.create(mockedField, actualValue));
        }
    }
}
