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

package com.ejisto.core.classloading.javassist;

import com.ejisto.core.classloading.ognl.OgnlAdapter;
import com.ejisto.core.classloading.proxy.EjistoProxyFactory;
import com.ejisto.modules.dao.entities.MockedField;
import com.ejisto.modules.factory.ObjectFactory;
import com.ejisto.modules.repository.MockedFieldsRepository;
import com.ejisto.modules.repository.ObjectFactoryRepository;
import ognl.OgnlContext;
import org.apache.log4j.Logger;

import static com.ejisto.constants.StringConstants.EJISTO_CLASS_TRANSFORMER_CATEGORY;

public class PropertyManager {

    private static final PropertyManager INSTANCE = new PropertyManager();
    private static final Logger logger = Logger.getLogger(EJISTO_CLASS_TRANSFORMER_CATEGORY.getValue());
    private MockedFieldsRepository mockedFieldsRepository;
    private EjistoProxyFactory ejistoProxyFactory;
    private OgnlAdapter ognlAdapter;
    private ObjectFactoryRepository objectFactoryRepository;

    private PropertyManager() {
        this.ejistoProxyFactory = EjistoProxyFactory.getInstance();
        this.ognlAdapter = new OgnlAdapter(new OgnlContext(), ejistoProxyFactory);
        this.mockedFieldsRepository = MockedFieldsRepository.getInstance();
        this.objectFactoryRepository = ObjectFactoryRepository.getInstance();
    }

    public static PropertyManager getInstance() {
        return INSTANCE;
    }

    private <T> T getFieldValue(String contextPath, String className, String fieldName, Class<T> type, T actualValue) {
        try {
            trace(String.format("loading fields for %s@%s - %s", fieldName, className, contextPath));
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
            logger.error(new StringBuilder("Property ").append(fieldName).append(" of class ").append(className).append(
                    " not found. Returning ").append(actualValue).toString(),
                         e);
            return actualValue;
        }
    }

    @SuppressWarnings("unchecked")
    private <T> T evaluateResult(MockedField mockedField, Class<T> type, T actualValue) throws Exception {
        ObjectFactory<T> objectFactory = objectFactoryRepository.getObjectFactory(mockedField.getFieldType(),
                                                                                  mockedField.getContextPath());
        trace("ObjectFactory " + objectFactory.getClass().getName() + " supports random values creation: " + objectFactory.supportsRandomValuesCreation());
        return objectFactory.create(mockedField, actualValue);
    }

    private <T> T parseExpression(MockedField mockedField, Class<T> type) throws Exception {
        T instance = ejistoProxyFactory.proxyClass(type, mockedField);
        ognlAdapter.apply(instance, mockedField);
        return instance;
    }

    public static <T> T mockField(String contextPath, String fieldName, String className, Class<T> type, T actual) {
        trace("calling mockField with " + type + " value");
        return INSTANCE.getFieldValue(contextPath, className, fieldName, type, actual);
    }

    public static byte mockField(String contextPath, String fieldName, String className, byte actual) {
        trace("calling mockField with byte value");
        return INSTANCE.getFieldValue(contextPath, className, fieldName, Byte.class, actual);
    }

    public static short mockField(String contextPath, String fieldName, String className, short actual) {
        trace("calling mockField with short value");
        return INSTANCE.getFieldValue(contextPath, className, fieldName, Short.class, actual);
    }

    public static int mockField(String contextPath, String fieldName, String className, int actual) {
        trace("calling mockField with int value");
        return INSTANCE.getFieldValue(contextPath, className, fieldName, Integer.class, actual);
    }

    public static long mockField(String contextPath, String fieldName, String className, long actual) {
        trace("calling mockField with long value");
        return INSTANCE.getFieldValue(contextPath, className, fieldName, Long.class, actual);
    }

    public static float mockField(String contextPath, String fieldName, String className, float actual) {
        trace("calling mockField with float value");
        return INSTANCE.getFieldValue(contextPath, className, fieldName, Float.class, actual);
    }

    public static double mockField(String contextPath, String fieldName, String className, double actual) {
        trace("calling mockField with double value");
        return INSTANCE.getFieldValue(contextPath, className, fieldName, Double.class, actual);
    }

    public static char mockField(String contextPath, String fieldName, String className, char actual) {
        trace("calling mockField with char value");
        return INSTANCE.getFieldValue(contextPath, className, fieldName, Character.class, actual);
    }

    public static boolean mockField(String contextPath, String fieldName, String className, boolean actual) {
        trace("calling mockField with boolean value");
        return INSTANCE.getFieldValue(contextPath, className, fieldName, Boolean.class, actual);
    }

    public static byte[] mockField(String contextPath, String fieldName, String className, byte[] actual) {
        return INSTANCE.getFieldValue(contextPath, className, fieldName, byte[].class, actual);
    }

    public static short[] mockField(String contextPath, String fieldName, String className, short[] actual) {
        return INSTANCE.getFieldValue(contextPath, className, fieldName, short[].class, actual);
    }

    public static int[] mockField(String contextPath, String fieldName, String className, int[] actual) {
        return INSTANCE.getFieldValue(contextPath, className, fieldName, int[].class, actual);
    }

    public static long[] mockField(String contextPath, String fieldName, String className, long[] actual) {
        return INSTANCE.getFieldValue(contextPath, className, fieldName, long[].class, actual);
    }

    public static float[] mockField(String contextPath, String fieldName, String className, float[] actual) {
        trace("calling mockField with float value");
        return INSTANCE.getFieldValue(contextPath, className, fieldName, float[].class, actual);
    }

    public static double[] mockField(String contextPath, String fieldName, String className, double[] actual) {
        trace("calling mockField with double value");
        return INSTANCE.getFieldValue(contextPath, className, fieldName, double[].class, actual);
    }

    public static char[] mockField(String contextPath, String fieldName, String className, char[] actual) {
        trace("calling mockField with char value");
        return INSTANCE.getFieldValue(contextPath, className, fieldName, char[].class, actual);
    }

    public static boolean[] mockField(String contextPath, String fieldName, String className, boolean[] actual) {
        trace("calling mockField with boolean value");
        return INSTANCE.getFieldValue(contextPath, className, fieldName, boolean[].class, actual);
    }

    private static void trace(String s) {
        if (logger.isTraceEnabled()) logger.trace(s);
    }
}
