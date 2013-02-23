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
import com.ejisto.modules.dao.entities.MockedField;
import org.hamcrest.Matcher;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import static ch.lambdaj.Lambda.*;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.equalTo;

public class MockedFieldsDao extends BaseLocalDao implements com.ejisto.modules.dao.MockedFieldsDao {

    @Override
    public List<MockedField> loadAll() {
        List<MockedField> out = new LinkedList<>();
        for (String contextPath : getDatabase().getRegisteredContextPaths()) {
            out.addAll(getDatabase().getMockedFields(contextPath));
        }
        return out;
    }

    @Override
    public Collection<MockedField> loadContextPathFields(String contextPath) {
        return getDatabase().getMockedFields(contextPath);
    }

    @Override
    public List<MockedField> loadByContextPathAndClassName(String contextPath, String className) {
        return select(getDatabase().getMockedFields(contextPath),
                      having(on(MockedField.class).getClassName(), equalTo(className)));
    }

    @Override
    public int countByContextPathAndClassName(String contextPath, String className) {
        return loadByContextPathAndClassName(contextPath, className).size();
    }

    @Override
    public MockedField getMockedField(String contextPath, String className, String fieldName) {
        MockedField field = getSingleField(getDatabase().getMockedFields(contextPath), className, fieldName);
        if (field == null) {
            throw new ApplicationException("No mockedFields found.");
        }
        return field;
    }

    @Override
    public boolean update(final MockedField field) {
        Collection<MockedField> fields = getDatabase().getMockedFields(field.getContextPath());
        MockedField existing = getSingleField(fields, field.getClassName(), field.getFieldName());
        fields.remove(existing);
        fields.add(cloneField(field));
        tryToCommit();
        return true;
    }

    @Override
    public MockedField insert(final MockedField field) {
        MockedField newField = internalInsert(field);
        field.setId(newField.getId());
        tryToCommit();
        return field;
    }


    @Override
    public void insert(Collection<MockedField> mockedFields) {
        for (MockedField mockedField : mockedFields) {
            MockedField newField = internalInsert(mockedField);
            mockedField.setId(newField.getId());
        }
        tryToCommit();
    }

    @Override
    public boolean deleteContext(final String contextPath) {
        getDatabase().deleteContextPath(contextPath);
        tryToCommit();
        return true;
    }

    private MockedField cloneField(MockedField field) {
        try {
            return field.unwrap().clone();
        } catch (CloneNotSupportedException e) {
            throw new IllegalStateException(e);
        }
    }


    private MockedField getSingleField(Collection<MockedField> fields, String className, String fieldName) {
        return selectFirst(fields, allOf(having(on(MockedField.class).getClassName(), equalTo(className)),
                                         having(on(MockedField.class).getFieldName(), equalTo(fieldName))));
    }

    private MockedField internalInsert(MockedField field) {
        MockedField newField = cloneField(field);
        newField.setId(getDatabase().getNextMockedFieldsSequenceValue());
        getDatabase().getMockedFields(field.getContextPath()).add(newField);
        return newField;
    }
}
