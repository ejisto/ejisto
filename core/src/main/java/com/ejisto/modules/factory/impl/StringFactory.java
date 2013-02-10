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

import com.ejisto.modules.dao.entities.MockedField;
import com.ejisto.modules.factory.ObjectFactory;

import java.util.Random;

/**
 * Created by IntelliJ IDEA.
 * User: celestino
 * Date: Dec 5, 2010
 * Time: 5:19:03 PM
 */
public class StringFactory implements ObjectFactory<String> {

    private static final Random random = new Random();

    @Override
    public String getTargetClassName() {
        return "java.lang.String";
    }

    @Override
    public String create(MockedField m, String actualValue) {
        return m.getFieldValue();
    }

    @Override
    public boolean supportsRandomValuesCreation() {
        return true;
    }

    @Override
    public String createRandomValue() {
        StringBuilder builder = new StringBuilder();
        int size = random.nextInt(1024);
        for (int i = 0; i < size; i++) {
            builder.append("a");
        }
        return builder.toString();
    }

}
