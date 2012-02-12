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

import com.ejisto.modules.dao.entities.MockedField;
import com.ejisto.modules.dao.entities.MockedFieldImpl;
import com.ejisto.modules.factory.AbstractContainerFactory;
import com.ejisto.modules.factory.ObjectFactory;
import com.ejisto.modules.repository.MockedFieldsRepository;
import ognl.Ognl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.springframework.util.CollectionUtils.isEmpty;

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
        applyExpressions(value, m.getExpression(), elementObjectFactory, m, actualValue);
        return value;
    }

    @Override
    public boolean supportsRandomValuesCreation() {
        return false;
    }

    @Override
    public Collection<Y> createRandomValue() {
        return null;
    }

    private void applyExpressions(Collection<Y> in, String expression, ObjectFactory<Y> elementObjectFactory, MockedField mockedField, Collection<Y> actualValue) {
        try {
            int size = 10;
            if (expression != null) {
                String[] expressions = expression.split(";");
                String[] keyValue;
                for (String exp : expressions) {
                    keyValue = exp.split("=");
                    if (keyValue[0].equals("size")) size = Integer.parseInt(keyValue[1]);
                    else Ognl.setValue(keyValue[0], in, keyValue[1]);
                }
            }
            fillCollection(in, size, elementObjectFactory, mockedField, actualValue);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void fillCollection(Collection<Y> in, int size, ObjectFactory<Y> elementObjectFactory, MockedField mockedField, Collection<Y> actualValue) {
        List<MockedField> fields = MockedFieldsRepository.getInstance().load(mockedField.getContextPath(),
                                                                             mockedField.getFieldElementType());
        boolean emptyFields = isEmpty(fields);
        MockedField target = new MockedFieldImpl();
        target.setClassName(mockedField.getFieldElementType());
        target.setFieldType(mockedField.getFieldElementType());
        target.setContextPath(mockedField.getContextPath());

        if (emptyFields && !isEmpty(actualValue)) in.addAll(actualValue);
        if (emptyFields && !elementObjectFactory.supportsRandomValuesCreation()) return;
        Y firstValue = isEmpty(actualValue) ? null : actualValue.iterator().next();
        for (int i = in.size(); i < size; i++) {
            if (emptyFields) in.add(elementObjectFactory.createRandomValue());
            else in.add(elementObjectFactory.create(target, firstValue));
        }
    }
}
