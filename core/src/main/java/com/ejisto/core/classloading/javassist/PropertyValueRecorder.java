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

import com.ejisto.core.classloading.util.ReflectionUtils;
import org.apache.log4j.Logger;

import static com.ejisto.constants.StringConstants.EJISTO_CLASS_TRANSFORMER_CATEGORY;

/**
 * Created by IntelliJ IDEA.
 * User: celestino
 * Date: 9/20/12
 * Time: 7:09 PM
 */
public final class PropertyValueRecorder {

    private static final Logger logger = Logger.getLogger(EJISTO_CLASS_TRANSFORMER_CATEGORY.getValue());
    private static final PropertyValueRecorder INSTANCE = new PropertyValueRecorder();

    private PropertyValueRecorder() {

    }

    public static PropertyValueRecorder getInstance() {
        return INSTANCE;
    }

    private <T> void storeFieldValue(String contextPath, String className, String fieldName, Class<T> type, T actualValue) {
        try {
            trace(String.format("storing field values for %s@%s - %s", fieldName, className, contextPath));
        } catch (Exception e) {
            logger.error(
                    String.format("Property %s of class %s not found. Returning %s", fieldName, className, actualValue),
                    e);
        }
    }

    public static <T> void recordFieldValue(String contextPath, String fieldName, String className, Class<T> type, T actual) {
        trace("calling recordFieldValue with " + type + " value");
        INSTANCE.storeFieldValue(contextPath, className, fieldName, type, actual);
    }

    public static void recordFieldValue(String contextPath, String fieldName, String className, byte actual) {
        trace("calling recordFieldValue with byte value");
        INSTANCE.storeFieldValue(contextPath, className, fieldName, Byte.class, actual);
    }

    public static void recordFieldValue(String contextPath, String fieldName, String className, short actual) {
        trace("calling recordFieldValue with short value");
        INSTANCE.storeFieldValue(contextPath, className, fieldName, Short.class, actual);
    }

    public static void recordFieldValue(String contextPath, String fieldName, String className, int actual) {
        trace("calling recordFieldValue with int value");
        INSTANCE.storeFieldValue(contextPath, className, fieldName, Integer.class, actual);
    }

    public static void recordFieldValue(String contextPath, String fieldName, String className, long actual) {
        trace("calling recordFieldValue with long value");
        INSTANCE.storeFieldValue(contextPath, className, fieldName, Long.class, actual);
    }

    public static void recordFieldValue(String contextPath, String fieldName, String className, float actual) {
        trace("calling recordFieldValue with float value");
        INSTANCE.storeFieldValue(contextPath, className, fieldName, Float.class, actual);
    }

    public static void recordFieldValue(String contextPath, String fieldName, String className, double actual) {
        trace("calling recordFieldValue with double value");
        INSTANCE.storeFieldValue(contextPath, className, fieldName, Double.class, actual);
    }

    public static void recordFieldValue(String contextPath, String fieldName, String className, char actual) {
        trace("calling recordFieldValue with char value");
        INSTANCE.storeFieldValue(contextPath, className, fieldName, Character.class, actual);
    }

    public static void recordFieldValue(String contextPath, String fieldName, String className, boolean actual) {
        trace("calling recordFieldValue with boolean value");
        INSTANCE.storeFieldValue(contextPath, className, fieldName, Boolean.class, actual);
    }

    private static void trace(String s) {
        if (logger.isTraceEnabled()) {
            logger.trace(s);
        }
    }
}
