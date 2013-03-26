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

package com.ejisto.modules.dao.local;

import ch.lambdaj.Lambda;
import com.ejisto.core.ApplicationException;
import com.ejisto.modules.dao.db.util.MockedFieldContainer;
import com.ejisto.modules.dao.db.util.MockedFieldExtractor;
import com.ejisto.modules.dao.entities.MockedField;
import com.ejisto.modules.dao.entities.MockedFieldImpl;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.concurrent.Callable;

import static ch.lambdaj.Lambda.*;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.beans.HasPropertyWithValue.hasProperty;

public class MockedFieldsDao extends BaseLocalDao implements com.ejisto.modules.dao.MockedFieldsDao {

    @Override
    public List<MockedField> loadAll() {
        List<MockedField> out = new LinkedList<>();
        for (String contextPath : getDatabase().getRegisteredContextPaths()) {
            out.addAll(Lambda.<MockedField>flatten(loadContextPathFields(contextPath)));
        }
        return out;
    }

    @Override
    public Collection<MockedField> loadContextPathFields(String contextPath) {
        return flatten(convert(getDatabase().getMockedFields(contextPath), new MockedFieldExtractor()));
    }

    @Override
    public List<MockedField> loadByContextPathAndClassName(String contextPath, String className) {
        Collection<MockedFieldContainer> fields = getMockedFieldsByClassName(contextPath, className);
        if (CollectionUtils.isEmpty(fields)) {
            return Collections.emptyList();
        }
        return convert(fields, new MockedFieldExtractor());
    }

    @Override
    public int countByContextPathAndClassName(String contextPath, String className) {
        return getMockedFieldsByClassName(contextPath, className).size();
    }

    @Override
    public MockedField getMockedField(String contextPath, String className, String fieldName) {
        MockedFieldContainer field = getSingleField(getMockedFieldsByClassName(contextPath, className), fieldName);
        if (field == null) {
            throw new ApplicationException("No mockedFields found.");
        }
        return field.getMockedField();
    }

    @Override
    public boolean exists(String contextPath, String className, String fieldName) {
        return getSingleField(getMockedFieldsByClassName(contextPath, className), fieldName) != null;
    }

    @Override
    public boolean update(final MockedField field) {
        transactionalOperation(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                Collection<MockedFieldContainer> fields = getMockedFieldsByClassName(field.getContextPath(),
                                                                                     field.getClassName());
                MockedFieldContainer existing = getSingleField(fields, field.getFieldName());
                Objects.requireNonNull(existing);
                fields.remove(existing);
                fields.add(MockedFieldContainer.from(field));
                return null;
            }
        });
        return true;
    }

    @Override
    public MockedField insert(final MockedField field) {
        transactionalOperation(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                MockedField newField = internalInsert(field);
                field.setId(newField.getId());
                return null;
            }
        });
        return field;
    }


    @Override
    public void insert(final Collection<MockedField> mockedFields) {
        transactionalOperation(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                for (MockedField mockedField : mockedFields) {
                    MockedField newField = internalInsert(mockedField);
                    mockedField.setId(newField.getId());
                }
                return null;
            }
        });
    }

    @Override
    public boolean createContext(final String contextPath) {
        transactionalOperation(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                if (!getDatabase().getRegisteredContextPaths().contains(contextPath)) {
                    getDatabase().registerContextPath(contextPath);
                }
                return null;
            }
        });
        return true;
    }

    @Override
    public boolean deleteContext(final String contextPath) {
        transactionalOperation(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                getDatabase().deleteAllMockedFields(contextPath);
                return null;
            }
        });
        return true;
    }

    private MockedField cloneField(MockedField field) {
        return MockedFieldImpl.copyOf((MockedFieldImpl) field.unwrap());
    }


    private MockedFieldContainer getSingleField(Collection<MockedFieldContainer> fields, String fieldName) {
        return selectFirst(fields, hasProperty("fieldName", equalTo(fieldName)));
    }

    private MockedField internalInsert(MockedField field) {
        MockedField newField = cloneField(field);
        newField.setId(getDatabase().getNextMockedFieldsSequenceValue());
        NavigableSet<MockedFieldContainer> container = getDatabase().getMockedFields(field.getContextPath());
        if (container == null) {
            getDatabase().registerContextPath(field.getContextPath());
            container = getDatabase().getMockedFields(field.getContextPath());
        }
        container.add(new MockedFieldContainer(field.getClassName(), field.getFieldName(), field));
        return newField;
    }

    private NavigableSet<MockedFieldContainer> getMockedFieldsByContextPath(String contextPath) {
        return getDatabase().getMockedFields(contextPath);
    }

    private Collection<MockedFieldContainer> getMockedFieldsByClassName(String contextPath, String className) {
        return select(getMockedFieldsByContextPath(contextPath),
                      hasProperty("className", equalTo(className)));
    }
}
