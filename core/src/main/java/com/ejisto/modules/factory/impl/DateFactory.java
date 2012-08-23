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

import java.util.Date;

import static java.lang.System.currentTimeMillis;
import static java.util.concurrent.TimeUnit.DAYS;

/**
 * Created by IntelliJ IDEA.
 * User: celestino
 * Date: 3/28/12
 * Time: 9:29 PM
 */
public class DateFactory implements ObjectFactory<Date> {

    public String getTargetClassName() {
        return "java.util.Date";
    }

    public Date create(MockedField m, Date actualValue) {
        return new Date(Long.parseLong(m.getFieldValue()));
    }

    public boolean supportsRandomValuesCreation() {
        return true;
    }

    /**
     * Returns a random date d where (today - 3 years) < d < (today + 3 years)
     *
     * @return random date
     */
    public Date createRandomValue() {
        return new Date(currentTimeMillis() - DAYS.toMillis(getRandomInterval()) + DAYS.toMillis(getRandomInterval()));
    }

    public long getRandomInterval() {
        return (long) Math.random() * 365 * 3;
    }
}
