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

package com.ejisto.modules.dao;

import com.ejisto.modules.dao.entities.MockedField;

import java.util.Collection;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: celestino
 * Date: 6/30/12
 * Time: 10:59 AM
 */
public interface MockedFieldsDao extends Dao {

    List<MockedField> loadAll();

    Collection<MockedField> loadContextPathFields(String contextPath);

    List<MockedField> loadByContextPathAndClassName(String contextPath, String className);

    int countByContextPathAndClassName(String contextPath, String className);

    MockedField getMockedField(String contextPath, String className, String fieldName);

    boolean update(MockedField field);

    long insert(MockedField field);

    void insert(Collection<MockedField> mockedFields);

    boolean deleteContext(String contextPath);
}
