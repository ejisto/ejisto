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

package com.ejisto.core.classloading.util;

import javassist.CtClass;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.apache.commons.lang3.StringUtils.*;

public abstract class ReflectionUtils {

    private static final Pattern GETTER = Pattern.compile("^((get)|(is)).*?$");
    private static final Pattern FIELD_EXTRACTOR = Pattern.compile("^((get)|(is)|(set)).*?$");
    private static final Pattern ARRAY_MATCHER = Pattern.compile("(\\[L([a-zA-Z0-9\\.]+);)|([a-zA-Z0-9\\.]+\\[\\])");
    private static final Pattern TYPE_EXTRACTOR = Pattern.compile("\\[?L?([a-zA-Z0-9\\.]+);?\\[?\\]?");
    private static final Map<String, String> primitives = new HashMap<String, String>();

    static {
        //populating primitive types map. By hand autoboxing/unboxing
        primitives.put("int", "java.lang.Integer");
        primitives.put("long", "java.lang.Long");
        primitives.put("char", "java.lang.Character");
        primitives.put("byte", "java.lang.Byte");
        primitives.put("boolean", "java.lang.Boolean");
        primitives.put("double", "java.lang.Double");
        primitives.put("float", "java.lang.Float");
        primitives.put("short", "java.lang.Short");
    }


    public static String getFieldName(String methodName) {
        if (isGetter(methodName) || isSetter(methodName)) {
            return extractFieldName(methodName);
        }
        return null;
    }

    public static boolean isGetter(String methodName) {
        return GETTER.matcher(methodName).matches();
    }

    public static boolean isGetterForProperty(String methodName, String propertyName) {
        return isGetter(methodName) && extractFieldName(methodName).equals(propertyName);
    }

    public static boolean isSetter(String methodName) {
        return methodName.startsWith("set");
    }

    public static <T> boolean hasStringConstructor(Class<T> type) {
        return Number.class.isAssignableFrom(type) || String.class.isAssignableFrom(type);
    }

    public static void detach(CtClass... classes) {
        for (CtClass clazz : classes) {
            detachClass(clazz);
        }
    }

    public static void detachClass(CtClass clazz) {
        try {
            clazz.detach();
        } catch (Exception ignore) {
        }
    }

    public static boolean isArray(String type) {
        return ARRAY_MATCHER.matcher(type).matches();
    }

    private static Pattern GENERIC_ELEMENT_EXTRACTOR = Pattern.compile(
            "\\??\\s?(extends)?(super)?\\s?([A-Za-z0-9\\._<>]*)");
    private static Pattern JAVASSIST_GENERIC_SIGNATURE = Pattern.compile("[A-Za-z0-9\\._]+\\s?<(.+?)>");

    public static String cleanGenericSignature(String signature) {
        String genericSignature = signature.trim();
        Matcher m = JAVASSIST_GENERIC_SIGNATURE.matcher(genericSignature);
        if (m.matches()) {
            genericSignature = m.group(1);
        }
        m = GENERIC_ELEMENT_EXTRACTOR.matcher(genericSignature);
        List<String> result = new ArrayList<String>();
        String match;
        while (m.find()) {
            match = m.group(3);
            if (isNotBlank(match)) {
                result.add(m.group(3));
            }
        }
        return join(result, ", ");
    }

    public static String getActualType(String type) {
        String actualType = type;
        Matcher m = TYPE_EXTRACTOR.matcher(actualType);
        if (m.matches()) {
            actualType = m.group(1);
        }
        if (primitives.containsKey(actualType)) {
            return primitives.get(actualType);
        }
        return actualType;
    }

    private static String extractFieldName(String methodName) {
        Matcher m = FIELD_EXTRACTOR.matcher(methodName);
        if (!m.matches()) {
            return null;
        }
        return uncapitalize(methodName.substring(m.end(1)));
    }


}
