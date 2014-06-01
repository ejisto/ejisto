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

package com.ejisto.modules.validation;

import com.ejisto.modules.dao.entities.MockedField;
import com.ejisto.modules.repository.ClassPoolRepository;
import javassist.ClassPool;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class MockedFieldValidator {

    private static final Map<String, Pattern> validatorMap;

    static {
        validatorMap = new HashMap<>();
        validatorMap.put("java.lang.String", Pattern.compile("^.*$"));
        validatorMap.put("java.lang.Byte", Pattern.compile("^-?\\d{1,3}$"));
        validatorMap.put("byte", Pattern.compile("^-?\\d{1,3}$"));
        validatorMap.put("java.lang.Short", Pattern.compile("^-?\\d{1,5}$"));
        validatorMap.put("short", Pattern.compile("^-?\\d{1,5}$"));
        validatorMap.put("java.lang.Integer", Pattern.compile("^-?\\d{1,9}$"));
        validatorMap.put("int", Pattern.compile("^-?\\d{1,9}$"));
        validatorMap.put("java.lang.Long", Pattern.compile("^-?\\d{1,19}$"));
        validatorMap.put("long", Pattern.compile("^-?\\d{1,19}$"));
        validatorMap.put("java.lang.Double", Pattern.compile("^-?\\d+?(\\.\\d+)*$"));
        validatorMap.put("double", Pattern.compile("^-?\\d+?(\\.\\d+)*$"));
        validatorMap.put("java.lang.Float", Pattern.compile("^-?\\d+?(\\.\\d+)*$"));
        validatorMap.put("float", Pattern.compile("^-?\\d+?(\\.\\d+)*$"));
    }

    public MockedFieldValidator() {
    }

    public boolean validate(Object target) {
        MockedField field = (MockedField) target;
        String type = field.getFieldType();
        if (validatorMap.containsKey(type)) {
            if (!validatorMap.get(type).matcher(field.getFieldValue()).matches()) {
                return false;
            }
        }
        final ClassPool classPool = ClassPoolRepository.getRegisteredClassPool(field.getContextPath());
        return classPool.getOrNull(type) != null;
    }
}
