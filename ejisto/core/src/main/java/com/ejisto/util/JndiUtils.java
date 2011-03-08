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

package com.ejisto.util;

import com.ejisto.modules.dao.entities.JndiDataSource;
import org.apache.commons.dbcp.BasicDataSource;
import org.springframework.jndi.JndiTemplate;

import javax.naming.CompoundName;
import javax.naming.Name;
import javax.naming.NameParser;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.util.Hashtable;
import java.util.List;
import java.util.Properties;

public class JndiUtils {

    private static Properties environment;
    private static Hashtable<String, String> envTable;

    private JndiUtils() {
    }

    static {
        environment = new Properties();
        environment.put(javax.naming.InitialContext.INITIAL_CONTEXT_FACTORY,
                        "org.eclipse.jetty.jndi.InitialContextFactory");
        envTable = new Hashtable<String, String>();
        envTable.put(javax.naming.InitialContext.INITIAL_CONTEXT_FACTORY,
                     "org.eclipse.jetty.jndi.InitialContextFactory");
    }

    public static void bindResources(List<JndiDataSource> entries) throws Exception {
        for (JndiDataSource entry : entries) {
            bindResource(entry);
        }
    }

    public static void bindResource(JndiDataSource entry) throws NamingException {
        bindResource(entry.getName(), createDataSource(entry));
    }

    public static void bindResource(String name, Object value) throws NamingException {
        //TODO implement container configuration
        JndiTemplate template = new JndiTemplate(environment);
        //checkParentResources(name, template);
        if (isAlreadyBound(name)) template.rebind(name, value);
        else template.bind(name, value);
    }

    public static DataSource getBoundDataSource(String resourceName) {
        JndiTemplate template = new JndiTemplate(environment);
        try {
            return template.lookup(resourceName, DataSource.class);
        } catch (Exception ex) {
            return null;
        }
    }

    public static boolean isAlreadyBound(String resourceName) {
        return getBoundDataSource(resourceName) != null;
    }

    private static DataSource createDataSource(JndiDataSource dataSourceEnvEntry) {
        BasicDataSource ds = new BasicDataSource();
        ds.setDriverClassName(dataSourceEnvEntry.getDriverClassName());
        ds.setUrl(dataSourceEnvEntry.getUrl());
        ds.setUsername(dataSourceEnvEntry.getUsername());
        ds.setPassword(dataSourceEnvEntry.getPassword());
        ds.setMaxActive(dataSourceEnvEntry.getMaxActive());
        ds.setMaxIdle(dataSourceEnvEntry.getMaxIdle());
        ds.setMaxWait(dataSourceEnvEntry.getMaxWait());
        return ds;
    }

    private static class DefaultParser implements NameParser {
        Properties syntax = new Properties();

        DefaultParser() {
            syntax.put("jndi.syntax.direction", "left_to_right");
            syntax.put("jndi.syntax.separator", "/");
            syntax.put("jndi.syntax.ignorecase", "false");
        }

        public Name parse(String name) throws NamingException {
            return new CompoundName(name, syntax);
        }
    }

}
