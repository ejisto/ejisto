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

package com.ejisto.hello.factory;

import com.ejisto.hello.beans.SimplePropertyValue;
import com.ejisto.modules.dao.entities.MockedField;
import com.ejisto.modules.factory.ObjectFactory;

import java.security.SecureRandom;

/**
 * Created by IntelliJ IDEA.
 * User: celestino
 * Date: 1/15/11
 * Time: 8:41 PM
 */
public class SimplePropertyValueFactory implements ObjectFactory<SimplePropertyValue> {

    private String[] values = new String[]{"first", "second", "third", "fourth", "fifth", "sixth", "seventh", "eighth", "ninth", "tenth"};
    private static final SecureRandom random = new SecureRandom();

    @Override
    public String getTargetClassName() {
        return "com.ejisto.hello.beans.SimplePropertyValue";
    }

    @Override
    public SimplePropertyValue create(MockedField m, SimplePropertyValue actualValue) {
        return createRandomValue();
    }

    @Override
    public boolean supportsRandomValuesCreation() {
        return true;
    }

    @Override
    public SimplePropertyValue createRandomValue() {
        return new SimplePropertyValue(values[random.nextInt(values.length)]);
    }
}
