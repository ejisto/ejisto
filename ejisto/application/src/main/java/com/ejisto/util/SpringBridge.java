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

package com.ejisto.util;

import static java.util.Collections.emptyList;

import java.util.List;
import java.util.Locale;

import javax.annotation.Resource;

import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;

import com.ejisto.event.EventManager;
import com.ejisto.modules.dao.MockedFieldsDao;
import com.ejisto.modules.dao.entities.MockedField;

public class SpringBridge implements ApplicationContextAware {
    private static SpringBridge instance = new SpringBridge();
    private ApplicationContext applicationContext;
    @Resource
    private MessageSource messageSource;
    @Resource
    private MockedFieldsDao mockedFieldsDao;

    public static boolean publishApplicationEvent(ApplicationEvent e) {
        boolean ret = isApplicationInitialized();
        if (ret)
            getInstance().getBean("eventManager", EventManager.class).publishEvent(e);
        return ret;
    }

    static String getMessage(String key, String language, Object... values) {
        if (!isApplicationInitialized())
            return "!!Application not initialized!!";
        return getInstance().internalGetMessage(key, language, values);
    }

    public static List<MockedField> getAllMockedFields() {
        if(!isApplicationInitialized()) return emptyList();
        return getInstance().loadAllMockedFields();
    }
    
    public static boolean updateMockedField(MockedField mockedField) {
        if(!isApplicationInitialized()) return false;
        return getInstance().internalUpdateMockedField(mockedField);
    }
    
    public static MockedField insertMockedField(MockedField mockedField) {
        if(!isApplicationInitialized()) return mockedField;
        return getInstance().internalInsertMockedField(mockedField);
    }

    public static SpringBridge getInstance() {
        return instance;
    }

    private static boolean isApplicationInitialized() {
        return getInstance().applicationContext != null;
    }

    private SpringBridge() {
    }

    public <T> T getBean(String name, Class<T> type) {
        if (this.applicationContext != null)
            return this.applicationContext.getBean(name, type);
        else
            return null;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    private String internalGetMessage(String key, String language, Object... values) {
        try {
            return messageSource.getMessage(key, values, new Locale(language));
        } catch (NoSuchMessageException e) {
            return key;
        }
    }

    private List<MockedField> loadAllMockedFields() {
        return mockedFieldsDao.loadAll();
    }
    
    private boolean internalUpdateMockedField(MockedField mockedField) {
        return mockedFieldsDao.update(mockedField);
    }
    
    private MockedField internalInsertMockedField(MockedField mockedField) {
        long id = mockedFieldsDao.insert(mockedField);
        mockedField.setId(id);
        return mockedField;
    }

}
