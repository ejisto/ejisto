/*
 * Ejisto, a powerful developer assistant
 *
 * Copyright (C) 2010  Celestino Bellone
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

package com.ejisto.event.listener;

import com.ejisto.event.def.MockedFieldChanged;
import com.ejisto.modules.dao.MockedFieldsDao;
import com.ejisto.modules.dao.entities.MockedField;
import org.springframework.context.ApplicationListener;

import javax.annotation.Resource;


public class FieldsUpdateListener implements ApplicationListener<MockedFieldChanged> {

    @Resource
    private MockedFieldsDao mockedFieldsDao;

    @Override
    public void onApplicationEvent(MockedFieldChanged event) {
        MockedField field = event.getMockedField();
        if(field.getId() > 0) mockedFieldsDao.update(field);
        else mockedFieldsDao.insert(field);
    }

}
