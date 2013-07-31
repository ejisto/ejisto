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

package com.ejisto.modules.factory.impl;

import com.ejisto.core.ApplicationException;
import com.ejisto.core.classloading.util.AutoGrowingList;
import com.ejisto.modules.dao.entities.MockedField;
import com.ejisto.modules.dao.entities.MockedFieldImpl;
import com.ejisto.modules.factory.AbstractContainerFactory;
import com.ejisto.modules.factory.ObjectFactory;
import com.ejisto.modules.repository.MockedFieldsRepository;
import com.ejisto.modules.web.util.ConfigurationManager;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import static org.apache.commons.collections.CollectionUtils.isEmpty;

/**
 * Created by IntelliJ IDEA.
 * User: celestino
 * Date: 1/15/11
 * Time: 7:41 PM
 */
public class CollectionFactory<Y> extends AbstractContainerFactory<Collection<Y>, Y> {

    private static final int MAX_SIZE = 10;
    private final MockedFieldsRepository mockedFieldsRepository;

    public CollectionFactory() {
        super();
        this.mockedFieldsRepository = new MockedFieldsRepository(null);
    }

    @Override
    public Class<Collection> getTargetClass() {
        return Collection.class;
    }

    @Override
    public String getTargetClassName() {
        return "java.util.Collection";
    }

    @Override
    public Collection<Y> create(MockedField m, Collection<Y> actualValue) {
        ObjectFactory<Y> elementObjectFactory = loadElementObjectFactory(m.getFieldElementType(), m.getContextPath());
        Collection<Y> value = newInstance(m);
        applyExpressions(value, m.getExpression(), elementObjectFactory, m, actualValue);
        fillCollection(value, elementObjectFactory, m, actualValue);
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

    @SuppressWarnings("unchecked")
    private Collection<Y> newInstance(MockedField m) {
        try {
            return (Collection<Y>) CollectionType.findByType(Class.forName(m.getFieldType())).newInstance();
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }


    private void applyExpressions(Collection<Y> in, String expression, ObjectFactory<Y> elementObjectFactory, MockedField mockedField, Collection<Y> actualValue) {
        if (StringUtils.isBlank(expression)) {
            return;
        }
        try {
            final ObjectMapper mapper = new ObjectMapper();
            final JsonNode root = mapper.readTree(expression.getBytes(ConfigurationManager.UTF_8));
            for (JsonNode child : root) {
                @SuppressWarnings("unchecked")
                Y element = (Y) mapper.readValue(child.asText(), elementObjectFactory.getTargetClass());
                in.add(element);
            }
        } catch (Exception e) {
            throw new ApplicationException(e);
        }
    }

    private void fillCollection(Collection<Y> in, ObjectFactory<Y> elementObjectFactory, MockedField mockedField, Collection<Y> actualValue) {
        List<MockedField> fields = mockedFieldsRepository.loadActiveFields(mockedField.getContextPath(),
                                                                           mockedField.getFieldElementType());
        boolean emptyFields = isEmpty(fields);
        MockedField target = new MockedFieldImpl();
        target.setClassName(mockedField.getFieldElementType());
        target.setFieldType(mockedField.getFieldElementType());
        target.setContextPath(mockedField.getContextPath());
        target.setActive(true);

        if (emptyFields && !isEmpty(actualValue)) {
            in.addAll(actualValue);
        }
        if (emptyFields && !elementObjectFactory.supportsRandomValuesCreation()) {
            return;
        }
        Y firstValue = isEmpty(actualValue) ? null : actualValue.iterator().next();
        for (int i = in.size(); i < (MAX_SIZE - (int) (MAX_SIZE * Math.random())); i++) {
            if (emptyFields) {
                in.add(elementObjectFactory.createRandomValue());
            } else {
                in.add(elementObjectFactory.create(target, firstValue));
            }
        }
    }

    private enum CollectionType {
        LIST(java.util.List.class) {
            @Override
            public Collection<?> newInstance() {
                return new AutoGrowingList<>();
            }
        },
        QUEUE(java.util.Queue.class) {
            @Override
            public Collection<?> newInstance() {
                return new LinkedList<>();
            }
        },
        SET(java.util.Set.class) {
            @Override
            public Collection<?> newInstance() {
                return new HashSet<>();
            }
        };
        private final Class<?> type;

        CollectionType(Class<?> type) {
            this.type = type;
        }

        abstract Collection<?> newInstance();

        static CollectionType findByType(Class<?> type) {
            for (CollectionType collectionType : values()) {
                if (collectionType.type.isAssignableFrom(type)) {
                    return collectionType;
                }
            }
            return LIST;
        }
    }

}
