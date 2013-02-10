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

package com.ejisto.modules.factory;

/**
 * Lists all the out-of-the-box supported types.
 * <p/>
 * Created by IntelliJ IDEA.
 * User: celestino
 * Date: 11/23/12
 * Time: 8:20 AM
 */
public enum DefaultSupportedType {

    ATOMIC_INTEGER(java.util.concurrent.atomic.AtomicInteger.class, true),
    ATOMIC_LONG(java.util.concurrent.atomic.AtomicLong.class, true),
    BOOLEAN(Boolean.class, true),
    NUMBER(Number.class, true),
    STRING(String.class, true),
    COLLECTION(java.util.Collection.class, false),
    MAP(java.util.Map.class, false),
    DATE(java.util.Date.class, false),
    LOCALE(java.util.Locale.class, true);

    private final Class<?> type;
    private final boolean primitiveOrSimpleValue;

    private DefaultSupportedType(Class<?> type, boolean primitiveOrSimpleValue) {
        this.type = type;
        this.primitiveOrSimpleValue = primitiveOrSimpleValue;
    }

    public Class<?> getType() {
        return type;
    }

    public boolean isPrimitiveOrSimpleValue() {
        return primitiveOrSimpleValue;
    }

    public String getName() {
        return type.getName();
    }

    public static DefaultSupportedType evaluate(Object obj) {
        for (DefaultSupportedType supportedType : values()) {
            if (supportedType.getType().isInstance(obj)) {
                return supportedType;
            }
        }
        return null;
    }
}
