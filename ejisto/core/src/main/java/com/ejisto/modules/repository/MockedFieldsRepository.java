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

package com.ejisto.modules.repository;

import ch.lambdaj.function.convert.Converter;
import com.ejisto.core.classloading.decorator.MockedFieldDecorator;
import com.ejisto.modules.dao.MockedFieldsDao;
import com.ejisto.modules.dao.entities.MockedField;
import com.ejisto.util.ExternalizableService;
import org.hamcrest.Matcher;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.List;

import static ch.lambdaj.Lambda.*;
import static org.hamcrest.Matchers.equalTo;

/**
 * Created by IntelliJ IDEA.
 * User: celestino
 * Date: Dec 3, 2010
 * Time: 7:51 PM
 */
public class MockedFieldsRepository extends ExternalizableService<MockedFieldsDao> {
    private static MockedFieldsRepository INSTANCE = new MockedFieldsRepository();

    @Resource
    private MockedFieldsDao mockedFieldsDao;
    private MockedFieldConverter mockedFieldConverter;

    public static MockedFieldsRepository getInstance() {
        return INSTANCE;
    }

    private MockedFieldsRepository() {
        mockedFieldConverter = new MockedFieldConverter();
    }

    public List<MockedField> loadAll(Matcher<MockedField> matcher) {
        List<MockedField> allFields = select(getMockedFieldsDao().loadAll(), matcher);
        return convert(allFields, mockedFieldConverter);
    }

    public List<MockedField> loadActiveFields(Matcher<MockedField> matcher) {
        return select(loadAll(matcher), having(on(MockedField.class).isActive(), equalTo(true)));
    }

    public MockedField load(String contextPath, String className, String fieldName) {
        return mockedFieldConverter.convert(getMockedFieldsDao().getMockedField(contextPath, className, fieldName));
    }

    public List<MockedField> load(String contextPath, String className) {
        return convert(getMockedFieldsDao().loadByContextPathAndClassName(contextPath, className),
                       mockedFieldConverter);
    }

    public boolean update(MockedField mockedField) {
        return getMockedFieldsDao().update(mockedField);
    }

    public MockedField insert(MockedField mockedField) {
        long id = getMockedFieldsDao().insert(mockedField);
        mockedField.setId(id);
        return mockedField;
    }

    public boolean isMockableClass(String contextPath, String className) {
        return getMockedFieldsDao().countByContextPathAndClassName(contextPath, className) > 0;
    }

    public boolean deleteContext(String contextPath) {
        return getMockedFieldsDao().deleteContext(contextPath);
    }

    public void insert(Collection<MockedField> fields) {
        getMockedFieldsDao().insert(fields);
    }

    private MockedFieldsDao getMockedFieldsDao() {
        checkDao();
        return getDaoInstance();
    }

    @Override
    protected MockedFieldsDao getDaoInstance() {
        return mockedFieldsDao;
    }

    @Override
    protected void setDaoInstance(MockedFieldsDao daoInstance) {
        this.mockedFieldsDao = daoInstance;
    }

    @Override
    protected Class<MockedFieldsDao> getDaoClass() {
        return MockedFieldsDao.class;
    }

    public List<MockedField> loadActiveFields(String contextPath, String className) {
        return select(mockedFieldsDao.loadByContextPathAndClassName(contextPath, className),
                      having(on(MockedField.class).isActive(), equalTo(true)));
    }

    private static final class MockedFieldConverter implements Converter<MockedField, MockedField> {
        @Override
        public MockedField convert(MockedField from) {
            return new MockedFieldDecorator(from);
        }
    }
}
