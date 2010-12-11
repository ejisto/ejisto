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

package com.ejisto.modules.factory.impl;

import com.ejisto.modules.dao.entities.MockedField;
import com.ejisto.modules.factory.ObjectFactory;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: celestino
 * Date: Dec 5, 2010
 * Time: 5:05:27 PM
 */
public class NumberObjectFactory implements ObjectFactory<Number> {

    private Map<String, Class<?>> registeredTypes = new HashMap<String, Class<?>>();

    public NumberObjectFactory() {
        //hand-made autoboxing...
        registeredTypes.put("char", Character.class);
        registeredTypes.put("short", Short.class);
        registeredTypes.put("byte", Byte.class);
        registeredTypes.put("int", Integer.class);
        registeredTypes.put("long", Long.class);
        registeredTypes.put("double", Double.class);
        registeredTypes.put("float", Float.class);
    }

    @Override
    public String getTargetClassName() {
        return "java.lang.Number";
    }

    @Override
    public Number create(MockedField m, Number actualValue) {
        try {
            Class<?> type = translateType(m.getFieldType());
            Constructor<?> constructor = type.getConstructor(String.class);
            return (Number) constructor.newInstance(m.getFieldValue());
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
    }

    private Class<?> translateType(String type) throws ClassNotFoundException {
        if (type.startsWith("java.")) return Class.forName(type);
        return registeredTypes.get(type);
    }


}
