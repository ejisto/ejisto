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

package com.ejisto.core.classloading.javassist;

import com.ejisto.core.classloading.proxy.EjistoProxyFactory;
import com.ejisto.core.classloading.util.ReflectionUtils;
import com.ejisto.modules.dao.entities.MockedField;
import com.ejisto.modules.factory.ObjectFactory;
import com.ejisto.modules.repository.MockedFieldsRepository;
import com.ejisto.modules.repository.ObjectFactoryRepository;
import org.apache.log4j.Logger;

import java.util.concurrent.atomic.AtomicReference;

import static com.ejisto.constants.StringConstants.EJISTO_CLASS_TRANSFORMER_CATEGORY;
import static com.ejisto.core.classloading.util.ReflectionUtils.getActualType;

public final class PropertyManager {

    private static final Logger logger = Logger.getLogger(EJISTO_CLASS_TRANSFORMER_CATEGORY.getValue());
    private static final AtomicReference<PropertyManager> REMOTE_INSTANCE = new AtomicReference<>();
    private final MockedFieldsRepository mockedFieldsRepository;
    private final ObjectFactoryRepository objectFactoryRepository;

    public PropertyManager(MockedFieldsRepository mockedFieldsRepository,
                            ObjectFactoryRepository objectFactoryRepository) {
        this.mockedFieldsRepository = mockedFieldsRepository;
        this.objectFactoryRepository = objectFactoryRepository;
    }

    public static PropertyManager newRemoteInstance() {
        PropertyManager remote = REMOTE_INSTANCE.get();
        if(remote != null) {
            return remote;
        }
        remote = new PropertyManager(new MockedFieldsRepository(null), new ObjectFactoryRepository(null, null));
        REMOTE_INSTANCE.compareAndSet(null, remote);
        return remote;
    }

    private <T> T getFieldValue(String contextPath, String className, String fieldName, Class<T> type, T actualValue) {
        try {
            trace(String.format("loading fields for %s@%s - %s", fieldName, className, contextPath));
            if (ReflectionUtils.getActualType(type.getName()).matches("^javax?\\..*?$")) {
                return getConcreteFieldValue(contextPath, className, fieldName, type, actualValue);
            } else {
                return getProxyOfFieldValue(contextPath, className, fieldName, type, actualValue);
            }
        } catch (Exception e) {
            logger.error(
                    String.format("Property %s of class %s not found. Returning %s", fieldName, className, actualValue),
                    e);
            return actualValue;
        }
    }

    private <T> T getProxyOfFieldValue(String contextPath, String className, String fieldName, Class<T> type, T actualValue) {
        try {
            String actualType = getActualType(type.getName());
            return EjistoProxyFactory.getInstance().proxyClass(actualType, contextPath);
        } catch (ClassNotFoundException e) {
            return actualValue;
        }
    }

    private <T> T getConcreteFieldValue(String contextPath, String className, String fieldName, Class<T> type, T actualValue) {
        try {
            MockedField mockedField = mockedFieldsRepository.load(contextPath, className, fieldName);
            trace("found " + mockedField);
            if (mockedField != null && mockedField.isActive()) {
                trace("mocked field is active");
                return evaluateResult(mockedField, type, actualValue);
            } else {
                trace("mocked field is not active");
                return actualValue;
            }
        } catch (Exception e) {
            logger.error(
                    String.format("Property %s of class %s not found. Returning %s", fieldName, className, actualValue),
                    e);
            return actualValue;
        }

    }

    @SuppressWarnings("unchecked")
    private <T> T evaluateResult(MockedField mockedField, Class<T> type, T actualValue) {
        ObjectFactory<T> objectFactory = objectFactoryRepository.getObjectFactory(mockedField.getFieldType(),
                                                                                  mockedField.getContextPath());
        trace("ObjectFactory " + objectFactory.getClass().getName() + " supports random values creation: " + objectFactory.supportsRandomValuesCreation());
        return objectFactory.create(mockedField, actualValue);
    }

    public <T> T mockField(String contextPath, String fieldName, String className, Class<T> type, T actual) {
        trace("calling mockField with " + type + " value");
        return getFieldValue(contextPath, className, fieldName, type, actual);
    }

    public byte mockField(String contextPath, String fieldName, String className, byte actual) {
        trace("calling mockField with byte value");
        return getFieldValue(contextPath, className, fieldName, Byte.class, actual);
    }

    public short mockField(String contextPath, String fieldName, String className, short actual) {
        trace("calling mockField with short value");
        return getFieldValue(contextPath, className, fieldName, Short.class, actual);
    }

    public int mockField(String contextPath, String fieldName, String className, int actual) {
        trace("calling mockField with int value");
        return getFieldValue(contextPath, className, fieldName, Integer.class, actual);
    }

    public long mockField(String contextPath, String fieldName, String className, long actual) {
        trace("calling mockField with long value");
        return getFieldValue(contextPath, className, fieldName, Long.class, actual);
    }

    public float mockField(String contextPath, String fieldName, String className, float actual) {
        trace("calling mockField with float value");
        return getFieldValue(contextPath, className, fieldName, Float.class, actual);
    }

    public double mockField(String contextPath, String fieldName, String className, double actual) {
        trace("calling mockField with double value");
        return getFieldValue(contextPath, className, fieldName, Double.class, actual);
    }

    public char mockField(String contextPath, String fieldName, String className, char actual) {
        trace("calling mockField with char value");
        return getFieldValue(contextPath, className, fieldName, Character.class, actual);
    }

    public boolean mockField(String contextPath, String fieldName, String className, boolean actual) {
        trace("calling mockField with boolean value");
        return getFieldValue(contextPath, className, fieldName, Boolean.class, actual);
    }

    private static void trace(String s) {
        if (logger.isTraceEnabled()) {
            logger.trace(s);
        }
    }
}
