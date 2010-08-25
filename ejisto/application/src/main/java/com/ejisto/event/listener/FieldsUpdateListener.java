/*
 * Copyright 2010 Celestino Bellone
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions
 * and limitations under the License.
 */

package com.ejisto.event.listener;

import javax.annotation.Resource;

import org.springframework.context.ApplicationListener;

import com.ejisto.event.def.MockedFieldChanged;
import com.ejisto.modules.dao.MockedFieldsDao;
import com.ejisto.modules.dao.entities.MockedField;


public class FieldsUpdateListener implements ApplicationListener<MockedFieldChanged> {

    @Resource
    private MockedFieldsDao mockedFieldsDao;

    @Override
    public void onApplicationEvent(MockedFieldChanged event) {
        MockedField field = event.getMockedField();
        if(field.getId() > 0) mockedFieldsDao.update(field);
        else mockedFieldsDao.update(field);
    }

}
