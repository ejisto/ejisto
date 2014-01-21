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

package com.ejisto.modules.repository;

import com.ejisto.core.classloading.decorator.MockedFieldDecorator;
import com.ejisto.modules.dao.MockedFieldsDao;
import com.ejisto.modules.dao.entities.MockedField;
import com.ejisto.modules.dao.remote.RemoteMockedFieldsDao;
import com.ejisto.modules.web.MockedFieldRequest;
import com.ejisto.util.ExternalizableService;
import lombok.extern.log4j.Log4j;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

import static java.util.stream.Collectors.toList;

/**
 * Created by IntelliJ IDEA.
 * User: celestino
 * Date: Dec 3, 2010
 * Time: 7:51 PM
 */
@Log4j
public final class MockedFieldsRepository extends ExternalizableService<MockedFieldsDao> {

    private static final Function<MockedField, MockedField> CONVERTER = (MockedFieldDecorator::from);

    public MockedFieldsRepository(MockedFieldsDao dao) {
        super(dao);
    }

    public List<MockedField> loadAll(Predicate<MockedField> matcher) {
        return getDao().loadAll().stream()
                .filter(matcher)
                .map(CONVERTER)
                .collect(toList());
    }

    public List<MockedField> loadAll(String contextPath, Predicate<MockedField> matcher) {
        return getDao().loadContextPathFields(contextPath).stream()
                .filter(matcher)
                .map(CONVERTER)
                .collect(toList());
    }

    public List<MockedField> loadAll() {
        return getDao().loadAll().stream().map(CONVERTER).collect(toList());
    }

    public List<MockedField> loadActiveFields(String contextPath, Predicate<MockedField> matcher) {
        return loadAll(contextPath, matcher).stream().filter(MockedField::isActive).collect(toList());
    }

    public MockedField load(String contextPath, String className, String fieldName) {
        return CONVERTER.apply(getDao().getMockedField(contextPath, className, fieldName));
    }

    public Optional<MockedField> loadOptional(String contextPath, String className, String fieldName) {
        if(exists(contextPath, className, fieldName)) {
            return Optional.of(CONVERTER.apply(getDao().getMockedField(contextPath, className, fieldName)));
        }
        return Optional.empty();
    }

    public boolean exists(String contextPath, String className, String fieldName) {
        return getDao().exists(contextPath, className, fieldName);
    }

    public List<MockedField> load(String contextPath, String className) {
        return getDao().loadByContextPathAndClassName(contextPath, className).stream()
                .map(CONVERTER)
                .collect(toList());
    }

    public boolean update(MockedField mockedField) {
        return getDao().update(mockedField);
    }

    public MockedField insert(MockedField mockedField) {
        return getDao().insert(mockedField);
    }

    public boolean isMockableClass(String contextPath, String className) {
        return getDao().countByContextPathAndClassName(contextPath, className) > 0;
    }

    public boolean createContext(String contextPath) {
        return getDao().createContext(contextPath);
    }

    public boolean deleteContext(String contextPath) {
        return getDao().deleteContext(contextPath);
    }

    public void insert(Collection<MockedField> fields) {
        getDao().insert(fields);
    }

    @Override
    protected MockedFieldsDao newRemoteDaoInstance() {
        return new RemoteMockedFieldsDao();
    }

    /**
     * Even if public, this is an internal method.
     *
     * @param contextPath requested Context Path
     * @param className requested Class Name
     * @return active fields matching selection criteria
     */
    public List<MockedField> loadActiveFields(String contextPath, String className) {
        return getDao().loadByContextPathAndClassName(contextPath, className).stream().filter(MockedField::isActive).collect(toList());
    }

    /**
     * Even if public, this is an internal method.
     *
     * @param request the selection criteria
     * @return all fields matching selection criteria
     */
    public Collection<MockedField> load(MockedFieldRequest request) {
        if (request.areAllContextPathFieldsRequested()) {
            return getDao().loadContextPathFields(request.getContextPath());
        } else if (request.areAllFieldsRequested()) {
            return loadAll();
        } else if (request.areAllClassPropertiesRequested()) {
            return loadActiveFields(request.getContextPath(), request.getClassName());
        }
        return Arrays.asList(load(request.getContextPath(), request.getClassName(), request.getFieldName()));
    }
}
