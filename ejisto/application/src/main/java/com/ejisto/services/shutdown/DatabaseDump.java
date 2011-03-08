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

package com.ejisto.services.shutdown;

import com.ejisto.constants.StringConstants;
import com.ejisto.modules.conf.SettingsManager;
import com.ejisto.modules.dao.*;
import com.ejisto.modules.dao.entities.*;
import com.ejisto.util.converter.ContainerDumpConverter;
import com.ejisto.util.converter.MockedFieldDumpConverter;
import org.apache.log4j.Logger;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.List;

import static ch.lambdaj.Lambda.convert;
import static com.ejisto.util.IOUtils.writeFile;
import static java.lang.String.format;

public class DatabaseDump extends BaseShutdownService {

    private static final Logger logger = Logger.getLogger(DatabaseDump.class);
    private static final String INSERT_SETTING = "INSERT INTO SETTINGS VALUES ('%s','%s');";
    private static final String INSERT_CONTEXT = "INSERT INTO WEBAPPLICATIONDESCRIPTOR(CONTEXTPATH,INSTALLATIONPATH) VALUES('%s','%s');";
    private static final String INSERT_ELEMENT = "INSERT INTO WEBAPPLICATIONDESCRIPTORELEMENT(CONTEXTPATH,PATH,KIND) VALUES('%s','%s','%s');";
    private static final String INSERT_DATASOURCE = "INSERT INTO JNDI_DATASOURCE (RESOURCENAME,RESOURCETYPE,DRIVERCLASSNAME,CONNECTIONURL,DRIVERJAR,USERNAME,PASSWORD,MAXACTIVE,MAXWAIT,MAXIDLE) VALUES(%s,%s,%s,%s,%s,%s,%s,%s,%s,%s);";
    private static final String NEWLINE = "\n";

    @Resource private MockedFieldsDao mockedFieldsDao;
    @Resource private SettingsDao settingsDao;
    @Resource private JndiDataSourcesDao jndiDataSourcesDao;
    @Resource private SettingsManager settingsManager;
    @Resource private WebApplicationDescriptorDao webApplicationDescriptorDao;
    @Resource private CustomObjectFactoryDao customObjectFactoryDao;
    @Resource private ContainersDao containersDao;

    @Override
    public void execute() {
        try {
            settingsManager.flush();
            StringBuilder file = new StringBuilder();
            dumpSettings(file);
            dumpMockedFields(file);
            dumpDescriptors(file);
            dumpDataSources(file);
            dumpContainers(file);
            writeFile(file.toString().getBytes(), System.getProperty(StringConstants.DERBY_SCRIPT.getValue()));
        } catch (Exception e) {
            logger.error("error during db dump", e);
        }
    }

    private void dumpSettings(StringBuilder file) {
        Collection<Setting> settings = settingsDao.loadAll();
        for (Setting setting : settings) {
            append(file, format(INSERT_SETTING, setting.getKey(), escape(setting.getValue())));
        }
    }

    private void dumpMockedFields(StringBuilder file) {
        Collection<MockedField> mockedFields = mockedFieldsDao.loadAll();
        append(file, convert(mockedFields, new MockedFieldDumpConverter()));
    }

    private void dumpContainers(StringBuilder file) {
        Collection<Container> containers = containersDao.loadAll();
        append(file, convert(containers, new ContainerDumpConverter()));
    }

    private void dumpDescriptors(StringBuilder file) {
        List<WebApplicationDescriptor> descriptors = webApplicationDescriptorDao.loadAll();
        for (WebApplicationDescriptor descriptor : descriptors) {
            file.append(format(INSERT_CONTEXT, descriptor.getContextPath(), descriptor.getInstallationPath())).append(
                    NEWLINE);
            for (WebApplicationDescriptorElement element : descriptor.getElements())
                file.append(
                        format(INSERT_ELEMENT, element.getContextPath(), element.getPath(), element.getKind())).append(
                        NEWLINE);
        }
    }

    private void dumpDataSources(StringBuilder file) {
        List<JndiDataSource> dataSources = jndiDataSourcesDao.loadAll();
        for (JndiDataSource dataSource : dataSources) {
            append(file, format(INSERT_DATASOURCE, escapeRaw(dataSource.getName()), escapeRaw(dataSource.getType()),
                                escapeRaw(dataSource.getDriverClassName()), escapeRaw(dataSource.getUrl()),
                                escapeRaw(dataSource.getDriverJarPath()), escapeRaw(dataSource.getUsername()),
                                escapeRaw(dataSource.getPassword()), dataSource.getMaxActive(), dataSource.getMaxWait(),
                                dataSource.getMaxIdle()));
        }
    }

    private void append(StringBuilder file, String text) {
        file.append(text).append(NEWLINE);
    }

    private void append(StringBuilder file, List<String> rows) {
        for (String s : rows) {
            append(file, s);
        }
    }

    private String escapeRaw(String in) {
        return in == null ? null : "'" + in.replaceAll("'", "''") + "'";
    }

    private String escape(String in) {
        return in == null ? null : in.replaceAll("'", "''");
    }

}
