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
import org.apache.commons.lang3.LocaleUtils;
import org.apache.commons.lang3.ObjectUtils;

import java.util.Locale;

/**
 * Created by IntelliJ IDEA.
 * User: celestino
 * Date: 12/3/12
 * Time: 6:40 PM
 */
public class LocaleFactory implements ObjectFactory<Locale> {

    @Override
    public String getTargetClassName() {
        return Locale.class.getName();
    }

    @Override
    public Locale create(MockedField m, Locale actualValue) {
        Locale locale;
        try {
            locale = LocaleUtils.toLocale(m.getFieldValue());
        } catch (IllegalArgumentException e) {
            locale = null;
        }
        return ObjectUtils.defaultIfNull(locale, actualValue);
    }

    @Override
    public boolean supportsRandomValuesCreation() {
        return false;
    }

    @Override
    public Locale createRandomValue() {
        return null;
    }
}
