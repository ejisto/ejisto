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

package com.ejisto.modules.factory.impl;

import com.ejisto.core.classloading.proxy.EjistoProxyFactory;
import com.ejisto.modules.dao.entities.MockedField;
import com.ejisto.modules.factory.ObjectFactory;
import lombok.extern.log4j.Log4j;

/**
 * Default Object factory
 */
@Log4j
public class DefaultObjectFactory implements ObjectFactory<Object> {

    @Override
    public String getTargetClassName() {
        return "java.lang.Object";
    }

    @Override
    public Object create(MockedField m, Object actualValue) {
        if (!m.isActive()) {
            return actualValue;
        }
        try {
            Class<?> clazz = Class.forName(m.getFieldType());
            return EjistoProxyFactory.getInstance().proxyClass(clazz, m.getContextPath());
        } catch (Exception e) {
            log.error("exception during field evaluation, returning actualValue.", e);
            return actualValue;
        }
    }

    @Override
    public boolean supportsRandomValuesCreation() {
        return false;
    }

    @Override
    public Object createRandomValue() {
        return null;
    }

}
