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
import org.apache.commons.collections4.CollectionUtils;

import java.util.*;
import java.util.concurrent.ForkJoinPool;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

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
            out.addAll(loadContextPathFields(contextPath));
        }
        out.addAll(forked.join());
        return out;
    }

    @Override
    public Collection<MockedField> loadContextPathFields(String contextPath) {
        return getDatabase().getMockedFields(contextPath).orElse(Collections.emptyNavigableSet()).stream().map(
                MockedFieldContainer::getMockedField).collect(Collectors.toList());
    }

    @Override
    public List<MockedField> loadByContextPathAndClassName(String contextPath, String className) {
        Collection<MockedFieldContainer> fields = getMockedFieldsByClassName(contextPath, className);
        if (CollectionUtils.isEmpty(fields)) {
            return Collections.emptyList();
        }
        return fields.stream().map(new MockedFieldExtractor()).collect(toList());
    }

    @Override
    public int countByContextPathAndClassName(String contextPath, String className) {
        return getMockedFieldsByClassName(contextPath, className).size();
    }

    @Override
    public MockedField getMockedField(String contextPath, String className, String fieldName) {
        return getSingleField(getMockedFieldsByClassName(contextPath, className), fieldName)
                .orElseThrow(ApplicationException.supplier("No mockedFields found."))
                .getMockedField();
    }

    @Override
    public boolean exists(String contextPath, String className, String fieldName) {
        return getSingleField(getMockedFieldsByClassName(contextPath, className), fieldName).isPresent();
    }

    @Override
    public void recordFieldCreation(final MockedField mockedField) {
        getDatabase().recordNewMockedFieldInsertion(mockedField);
    }

    @Override
    public List<MockedField> getRecentlyCreatedFields() {
        return getDatabase().getNewMockedFieldInsertion();
    }

    @Override
    public boolean update(final MockedField field) {
        transactionalOperation(() -> {
            Collection<MockedFieldContainer> fields = getMockedFieldsByClassName(field.getContextPath(),
                                                                                 field.getClassName());
            getSingleField(fields, field.getFieldName()).orElseThrow(IllegalArgumentException::new);
            updateField(field.unwrap());
            return null;
        });
        return true;
    }

    @Override
    public MockedField insert(final MockedField field) {
        return transactionalOperation(() -> internalInsert(field));
    }


    @Override
    public void insert(final Collection<MockedField> mockedFields) {
        transactionalOperation(() -> {
            mockedFields.stream().forEach(this::internalInsert);
            return null;
        });
    }

    @Override
    public boolean createContext(final String contextPath) {
        transactionalOperation(() -> {
            if (!getDatabase().getRegisteredContextPaths().contains(contextPath)) {
                getDatabase().registerContextPath(contextPath);
            }
            return null;
        });
        return true;
    }

    @Override
    public boolean deleteContext(final String contextPath) {
        transactionalOperation(() -> {
            getDatabase().deleteAllMockedFields(contextPath);
            return null;
        });
        return true;
    }

    private MockedField cloneField(MockedField field) {
        return MockedFieldImpl.copyOf((MockedFieldImpl) field.unwrap());
    }


    private Optional<MockedFieldContainer> getSingleField(Collection<MockedFieldContainer> fields, String fieldName) {
        return fields.stream().filter(f -> f.getFieldName().equals(fieldName)).findFirst();
    }

    private MockedField internalInsert(MockedField field) {
        return saveField(field, false);
    }

    private MockedField updateField(MockedField field) {
        return saveField(field, true);
    }

    private MockedField saveField(MockedField field, boolean update) {
        MockedField newField = cloneField(field);
        NavigableSet<MockedFieldContainer> container;
        Optional<NavigableSet<MockedFieldContainer>> result = getDatabase().getMockedFields(field.getContextPath());
        if (!result.isPresent()) {
            getDatabase().registerContextPath(field.getContextPath());
            container = getDatabase().getMockedFields(field.getContextPath()).orElseThrow(IllegalStateException::new);
        } else {
            container = result.get();
        }
        if(update) {
            container.removeIf(c -> c.wraps(newField));
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
