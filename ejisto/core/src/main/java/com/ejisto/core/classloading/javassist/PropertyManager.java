/*
 * Ejisto, a powerful developer assistant
 *
 * Copyright (C) 2010-2011  Celestino Bellone
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

package com.ejisto.core.classloading.javassist;

import com.ejisto.core.classloading.ognl.OgnlAdapter;
import com.ejisto.core.classloading.proxy.EjistoProxyFactory;
import com.ejisto.modules.dao.entities.MockedField;
import com.ejisto.modules.factory.ObjectFactory;
import com.ejisto.modules.repository.MockedFieldsRepository;
import com.ejisto.modules.repository.ObjectFactoryRepository;
import org.apache.log4j.Logger;

import javax.annotation.Resource;

public class PropertyManager {

    private static final PropertyManager INSTANCE = new PropertyManager();
    private static final Logger logger = Logger.getLogger(PropertyManager.class);
    @Resource
    private MockedFieldsRepository mockedFieldsRepository;
    @Resource
    private EjistoProxyFactory ejistoProxyFactory;
    @Resource
    private OgnlAdapter ognlAdapter;
    @Resource
    private ObjectFactoryRepository objectFactoryRepository;

    public static PropertyManager getInstance() {
        return INSTANCE;
    }

    private <T> T getFieldValue(String contextPath, String className, String fieldName, Class<T> type, T actualValue) {
        try {
            MockedField mockedField = mockedFieldsRepository.load(contextPath, className, fieldName);
            if (mockedField != null && mockedField.isActive()) {
                return evaluateResult(mockedField, type, actualValue);
            } else {
                return actualValue;
            }
        } catch (Exception e) {
            logger.error("Property " + fieldName + " of class " + className + " not found. Returning " + actualValue,
                         e);
            return actualValue;
        }
    }

    @SuppressWarnings("unchecked")
    private <T> T evaluateResult(MockedField mockedField, Class<T> type, T actualValue) throws Exception {
        String objectFactoryClass = objectFactoryRepository.getObjectFactory(mockedField.getFieldType(),
                                                                             mockedField.getContextPath());
        ObjectFactory<T> objectFactory = (ObjectFactory<T>) Thread.currentThread().getContextClassLoader().loadClass(
                objectFactoryClass).newInstance();
        return objectFactory.create(mockedField, actualValue);
    }

    private <T> T parseExpression(MockedField mockedField, Class<T> type) throws Exception {
        T instance = ejistoProxyFactory.proxyClass(type, mockedField);
        ognlAdapter.apply(instance, mockedField);
        return instance;
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
