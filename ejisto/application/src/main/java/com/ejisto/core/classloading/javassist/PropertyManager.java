/*******************************************************************************
 * Copyright 2010 Celestino Bellone
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package com.ejisto.core.classloading.javassist;

import java.lang.reflect.Constructor;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.InitializingBean;

import com.ejisto.modules.dao.MockedFieldsDao;
import com.ejisto.modules.dao.entities.MockedField;

public class PropertyManager implements InitializingBean {

    private static PropertyManager INSTANCE;
    private static final Logger logger = Logger.getLogger(PropertyManager.class);
    @Resource
    private MockedFieldsDao mockedFieldsDao;

    private <T> T getFieldValue(String contextPath, String className, String fieldName, Class<T> type, T actualValue) {
        try {
            MockedField mockedField = mockedFieldsDao.getMockedField(contextPath, className, fieldName);
            if (mockedField != null) {
                Constructor<T> constructor = type.getConstructor(String.class);
                return constructor.newInstance(mockedField.getFieldValue());
            } else {
                return actualValue;
            }
        } catch (Exception e) {
            logger.error("Property " + fieldName + " of class " + className + " not found. Returning " + actualValue, e);
            return actualValue;
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        INSTANCE = this;
    }

    public static <T> T mockField(String contextPath, String fieldName, String className, Class<T> type, T actual) {
        return INSTANCE.getFieldValue(contextPath, className, fieldName, type, actual);
    }

    public static byte mockField(String contextPath, String fieldName, String className, byte actual) {
        return INSTANCE.getFieldValue(contextPath, className, fieldName, Byte.class, actual);
    }

    public static short mockField(String contextPath, String fieldName, String className, short actual) {
        return INSTANCE.getFieldValue(contextPath, className, fieldName, Short.class, actual);
    }

    public static int mockField(String contextPath, String fieldName, String className, int actual) {
        return INSTANCE.getFieldValue(contextPath, className, fieldName, Integer.class, actual);
    }

    public static long mockField(String contextPath, String fieldName, String className, long actual) {
        return INSTANCE.getFieldValue(contextPath, className, fieldName, Long.class, actual);
    }

    public static float mockField(String contextPath, String fieldName, String className, float actual) {
        return INSTANCE.getFieldValue(contextPath, className, fieldName, Float.class, actual);
    }

    public static double mockField(String contextPath, String fieldName, String className, double actual) {
        return INSTANCE.getFieldValue(contextPath, className, fieldName, Double.class, actual);
    }

    public static char mockField(String contextPath, String fieldName, String className, char actual) {
        return INSTANCE.getFieldValue(contextPath, className, fieldName, Character.class, actual);
    }

    public static boolean mockField(String contextPath, String fieldName, String className, boolean actual) {
        return INSTANCE.getFieldValue(contextPath, className, fieldName, Boolean.class, actual);
    }
}
