/*
 * Ejisto, a powerful developer assistant
 *
 * Copyright (C) 2010-2011  Celestino Bellone
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
import com.ejisto.modules.dao.entities.JndiDataSource;

/**
 * Created by IntelliJ IDEA.
 * User: celestino
 * Date: 3/20/11
 * Time: 11:44 AM
 */
public class JndiDataSourceDumpConverter implements Converter<JndiDataSource, String> {
    private static final String INSERT = "INSERT INTO JNDI_DATASOURCE (RESOURCENAME,RESOURCETYPE,DRIVERCLASSNAME,CONNECTIONURL,DRIVERJAR,USERNAME,PASSWORD,MAXACTIVE,MAXWAIT,MAXIDLE) VALUES(%s,%s,%s,%s,%s,%s,%s,%s,%s,%s);";

    @Override
    public String convert(JndiDataSource dataSource) {
        return String.format(INSERT, escapeRaw(dataSource.getName()), escapeRaw(dataSource.getType()), escapeRaw(dataSource.getDriverClassName()),
                             escapeRaw(dataSource.getUrl()), escapeRaw(dataSource.getDriverJarPath()), escapeRaw(dataSource.getUsername()),
                             escapeRaw(dataSource.getPassword()), dataSource.getMaxActive(), dataSource.getMaxWait(), dataSource.getMaxIdle());
    }

    private String escapeRaw(String in) {
        return in == null ? null : "'" + in.replaceAll("'", "''") + "'";
    }

}
