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

package com.ejisto.modules.factory.impl;

import com.ejisto.modules.dao.entities.MockedField;
import com.ejisto.modules.factory.ObjectFactory;
import lombok.extern.log4j.Log4j;
import org.springframework.util.Assert;

/**
 * Created by IntelliJ IDEA.
 * User: celestino
 * Date: 3/22/12
 * Time: 8:08 AM
 */
@Log4j
public class EnumFactory<T extends Enum<T>> implements ObjectFactory<Enum<T>> {

    @Override
    public String getTargetClassName() {
        return "java.lang.Enum";
    }

    @Override
    public Enum<T> create(MockedField m, Enum<T> actualValue) {
        try {
            String name = m.getFieldValue();
            Assert.hasText(name);
            @SuppressWarnings("unchecked")
            Class<Enum<T>> clazz = (Class<Enum<T>>) Class.forName(m.getFieldType());
            Assert.state(clazz.isEnum());
            Enum<T>[] enums = clazz.getEnumConstants();
            for (Enum<T> en : enums) {
                if (en.name().equals(name)) return en;
            }
            if (actualValue != null) return actualValue;
            return enums.length > 0 ? enums[0] : null;
        } catch (Exception ex) {
            log.warn(String.format("enum value not found for %s.", m), ex);
        }
        return actualValue;
    }

    @Override
    public boolean supportsRandomValuesCreation() {
        return false;
    }

    @Override
    public Enum<T> createRandomValue() {
        return null;
    }

}
