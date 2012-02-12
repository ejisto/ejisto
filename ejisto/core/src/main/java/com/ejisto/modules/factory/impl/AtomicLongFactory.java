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

import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by IntelliJ IDEA.
 * User: celestino
 * Date: Dec 5, 2010
 * Time: 5:23:23 PM
 */
public class AtomicLongFactory implements ObjectFactory<AtomicLong> {

    @Override
    public String getTargetClassName() {
        return "java.util.concurrent.atomic.AtomicLong";
    }

    @Override
    public AtomicLong create(MockedField m, AtomicLong actualValue) {
        return new AtomicLong(Long.parseLong(m.getFieldValue()));
    }

    @Override
    public boolean supportsRandomValuesCreation() {
        return false;
    }

    @Override
    public AtomicLong createRandomValue() {
        return new AtomicLong();
    }
}
