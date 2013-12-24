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
import com.ejisto.modules.dao.MockedFieldsDao;
import com.ejisto.modules.dao.db.EmbeddedDatabaseManager;
import com.ejisto.modules.dao.db.util.MockedFieldContainer;
import com.ejisto.modules.dao.db.util.MockedFieldExtractor;
import com.ejisto.modules.dao.entities.MockedField;
import com.ejisto.modules.dao.entities.MockedFieldImpl;
import com.ejisto.modules.dao.local.helper.RecursiveMockedFieldLoader;
import com.ejisto.modules.recorder.CollectedData;
import com.ejisto.modules.web.MockedFieldRequest;
import org.apache.commons.collections.CollectionUtils;

import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ForkJoinPool;
import java.util.stream.Collectors;

import static ch.lambdaj.Lambda.*;
import static java.util.stream.Collectors.toList;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.beans.HasPropertyWithValue.hasProperty;

public class LocalMockedFieldsDao extends BaseLocalDao implements MockedFieldsDao {

    private final ForkJoinPool forkJoinPool = new ForkJoinPool();
    private final LocalCollectedDataDao collectedDataDao;

    public LocalMockedFieldsDao(EmbeddedDatabaseManager database, LocalCollectedDataDao collectedDataDao) {
        super(database);
        this.collectedDataDao = collectedDataDao;
    }

    @Override
    public List<MockedField> loadAll() {
        List<MockedField> out = new LinkedList<>();
        List<CollectedData> activeSessions = new ArrayList<>(getActiveRecordedSessions());
        RecursiveMockedFieldLoader forked = new RecursiveMockedFieldLoader(activeSessions, collectedDataDao,
                                                                           MockedFieldRequest.requestAllFields());
        forkJoinPool.execute(forked);
        for (String contextPath : getDatabase().getRegisteredContextPaths()) {
            out.addAll(Lambda.<MockedField>flatten(loadContextPathFields(contextPath)));
        }
        out.addAll(forked.join());
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
                fields.add(MockedFieldContainer.from(field.unwrap()));
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
                internalInsert(field);
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
                    internalInsert(mockedField);
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
        NavigableSet<MockedFieldContainer> container;
        Optional<NavigableSet<MockedFieldContainer>> result = getDatabase().getMockedFields(field.getContextPath());
        if (!result.isPresent()) {
            getDatabase().registerContextPath(field.getContextPath());
            container = getDatabase().getMockedFields(field.getContextPath()).orElse(Collections.emptyNavigableSet());
        } else {
            container = result.get();
        }
        container.add(MockedFieldContainer.from(field));
        return newField;
    }

    private NavigableSet<MockedFieldContainer> getMockedFieldsByContextPath(String contextPath) {
        return getDatabase().getMockedFields(contextPath).orElse(Collections.emptyNavigableSet());
    }

    private Collection<MockedFieldContainer> getMockedFieldsByClassName(String contextPath, String className) {
        return getMockedFieldsByContextPath(contextPath)
                .stream()
                .filter(field -> field.getClassName().equals(className))
                .collect(toList());
    }

    private Collection<CollectedData> getActiveRecordedSessions() {
        return getDatabase().getActiveRecordedSessions().orElse(Collections.emptyList());
    }
}
