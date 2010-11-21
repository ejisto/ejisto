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

package com.ejisto.core.classloading.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.springframework.util.StringUtils.uncapitalize;

public class ReflectionUtils {

    private static final Pattern GETTER = Pattern.compile("^((get)|(is)).*?$");
    private static final Pattern FIELD_EXTRACTOR = Pattern.compile("^((get)|(is)|(set)).*?$");

    public static String getFieldName(String methodName) {
        if(isGetter(methodName) || isSetter(methodName)) return extractFieldName(methodName);
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

    private static String extractFieldName(String methodName) {
        Matcher m = FIELD_EXTRACTOR.matcher(methodName);
        if(!m.matches()) return null;
        return uncapitalize(methodName.substring(m.end(1)));
    }

}
