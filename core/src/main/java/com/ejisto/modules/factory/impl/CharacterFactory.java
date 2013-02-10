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
import org.apache.commons.lang3.StringUtils;

import java.util.Random;

/**
 * Created by IntelliJ IDEA.
 * User: celestino
 * Date: 12/18/10
 * Time: 12:51 PM
 */
public class CharacterFactory implements ObjectFactory<Character> {

    private static final Random random = new Random();

    @Override
    public String getTargetClassName() {
        return "java.lang.Character";
    }

    @Override
    public Character create(MockedField m, Character actualValue) {
        if (StringUtils.isNotBlank(m.getFieldValue())) {
            return m.getFieldValue().charAt(0);
        }
        return actualValue != null ? actualValue : ' ';
    }

    @Override
    public boolean supportsRandomValuesCreation() {
        return true;
    }

    @Override
    public Character createRandomValue() {
        return (char) (random.nextInt(127) + 1);
    }
}
