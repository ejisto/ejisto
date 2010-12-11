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
import org.apache.log4j.Logger;

import static com.ejisto.core.classloading.util.ReflectionUtils.hasStringConstructor;

/**
 * Default Object factory
 */
public class DefaultObjectFactory implements ObjectFactory<Object> {
    private static final Logger logger = Logger.getLogger(DefaultObjectFactory.class);

    @Override
    public String getTargetClassName() {
        return "java.lang.Object";
    }

    @Override
    public Object create(MockedField m, Object actualValue) {
        try {
            Class<?> clazz = Class.forName(m.getFieldType());
            if (hasStringConstructor(clazz)) {
                clazz.getConstructor(String.class).newInstance(m.getFieldValue());
            }
            return clazz.newInstance();
        } catch (Exception e) {
            logger.error("exception during field evaluation, returning actualValue.", e);
            return actualValue;
        }
    }


}
