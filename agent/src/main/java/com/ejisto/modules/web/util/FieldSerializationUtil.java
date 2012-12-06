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

package com.ejisto.modules.web.util;

import com.ejisto.modules.dao.entities.MockedField;
import com.ejisto.modules.dao.entities.MockedFieldImpl;
import com.ejisto.modules.factory.DefaultSupportedType;
import lombok.extern.java.Log;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.ServletRequest;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Level;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;

/**
 * Created by IntelliJ IDEA.
 * User: celestino
 * Date: 11/23/12
 * Time: 8:14 AM
 */
@Log
public class FieldSerializationUtil {

    private static final ConcurrentMap<Integer, String> CACHED_IDS = new ConcurrentHashMap<Integer, String>();
    private static final List<Class<?>> BLACKLIST = new ArrayList<Class<?>>();
    static {
        BLACKLIST.add(ClassLoader.class);
    }

    public static List<MockedField> translateObject(Object object, String containerClassName, String fieldName, String contextPath) {
        if (object == null) {
            return emptyList();
        }
        if (isBlackListed(object.getClass())) {
            String fieldType = object.getClass().getName();
            MockedField out = buildMockedField(containerClassName, fieldName, contextPath, fieldType);
            return asList(out);
        }
        DefaultSupportedType type = DefaultSupportedType.evaluate(object);
        if (type != null && type.isPrimitiveOrSimpleValue()) {
            return asList(
                    getSingleFieldFromDefaultSupportedType(object, type, contextPath, containerClassName, fieldName));
        }
        List<MockedField> out = new ArrayList<MockedField>();
        Class<?> objectClass = object.getClass();
        String className = objectClass.getName();
        for (Field field : objectClass.getDeclaredFields()) {
            if (!Modifier.isTransient(field.getModifiers())) {
                out.add(translateField(field, object, className, contextPath));
            }
        }
        return out;
    }

    private static boolean isBlackListed(Class<?> clazz) {
        for (Class<?> blackListedClass : BLACKLIST) {
            if (blackListedClass.isAssignableFrom(clazz)) {
                return true;
            }
        }
        return false;
    }

    private static MockedField translateField(Field field, Object container, String className, String contextPath) {
        Class<?> fieldType = field.getType();
        MockedField out = buildMockedField(className, field.getName(), contextPath, fieldType.getName());
        Object value = safeGet(field, container);
        if (value == null || isBlackListed(fieldType)) {
            out.setFieldValue(null);
            return out;
        }
        int hashCode = System.identityHashCode(value);
        String existingClassMapping = CACHED_IDS.putIfAbsent(hashCode, out.getFieldType());
        if (StringUtils.isNotBlank(existingClassMapping)) {
            out.setLink(String.valueOf(hashCode));
            return out;
        } else if (field.isEnumConstant() || fieldType.isPrimitive()) {
            out.setFieldValue(String.valueOf(value));
        } else {
            out.setExpression(translateValue(value));
        }
        out.setRecordedObjectHashCode(hashCode);
        return out;
    }

    private static String translateValue(Object value) {
        try {
            return JSONUtil.encode(value, ClassLoader.class, ServletRequest.class);
        } catch (IllegalStateException ex) {
            log.log(Level.SEVERE, "exception during object serialization", ex);
            return null;
        }
    }

    private static Object safeGet(Field f, Object container) {
        try {
            f.setAccessible(true);
            return f.get(container);
        } catch (IllegalAccessException e) {
            log.log(Level.SEVERE, "unexpected exception", e);
            return null;
        }
    }

    private static MockedField getSingleFieldFromDefaultSupportedType(Object object, DefaultSupportedType type, String contextPath, String containerClassName, String fieldName) {
        MockedField out = buildMockedField(containerClassName, fieldName, contextPath, object.getClass().getName());
        out.setFieldValue(String.valueOf(object));
        return out;
    }

    private static MockedField buildMockedField(String containerClassName, String fieldName, String contextPath, String fieldType) {
        MockedField out = new MockedFieldImpl();
        out.setActive(true);
        out.setContextPath(contextPath);
        out.setClassName(containerClassName);
        out.setFieldName(fieldName);
        out.setFieldType(fieldType);
        return out;
    }


}
