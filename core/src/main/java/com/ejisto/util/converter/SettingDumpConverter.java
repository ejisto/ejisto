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

package com.ejisto.util.converter;

import ch.lambdaj.function.convert.Converter;
import com.ejisto.constants.StringConstants;
import com.ejisto.modules.dao.entities.Setting;

import java.util.EnumSet;

import static com.ejisto.constants.StringConstants.EJISTO_VERSION;

/**
 * Created by IntelliJ IDEA.
 * User: celestino
 * Date: 3/19/11
 * Time: 6:09 PM
 */
public class SettingDumpConverter implements Converter<Setting, String> {
    private static final String INSERT_SETTING = "INSERT INTO SETTINGS VALUES ('%s','%s');";
    private static final EnumSet<StringConstants> readOnlySettings = EnumSet.of(EJISTO_VERSION);

    @Override
    public String convert(Setting from) {
        if (readOnlySettings.contains(StringConstants.fromValue(from.getKey()))) {
            return "";
        }
        return String.format(INSERT_SETTING, from.getKey(), escape(from.getValue()));
    }

    private String escape(String in) {
        return in == null ? null : in.replaceAll("'", "''");
    }
}
