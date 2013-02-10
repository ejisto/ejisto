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

package com.ejisto.services.shutdown;

import com.ejisto.constants.StringConstants;
import com.ejisto.modules.conf.SettingsManager;
import com.ejisto.modules.dao.CustomObjectFactoryDao;
import com.ejisto.modules.dao.entities.*;
import com.ejisto.modules.dao.jdbc.ContainersDao;
import com.ejisto.modules.dao.jdbc.JndiDataSourcesDao;
import com.ejisto.modules.dao.jdbc.WebApplicationDescriptorDao;
import com.ejisto.util.converter.*;
import lombok.extern.log4j.Log4j;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Resource;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;
import java.util.List;
import java.util.zip.GZIPOutputStream;

import static ch.lambdaj.Lambda.*;

@Log4j
public class DatabaseDump extends BaseShutdownService {

    private static final char NEWLINE = '\n';

    @Resource private com.ejisto.modules.dao.MockedFieldsDao mockedFieldsDao;
    @Resource private com.ejisto.modules.dao.SettingsDao settingsDao;
    @Resource private JndiDataSourcesDao jndiDataSourcesDao;
    @Resource private SettingsManager settingsManager;
    @Resource private WebApplicationDescriptorDao webApplicationDescriptorDao;
    @Resource private CustomObjectFactoryDao customObjectFactoryDao;
    @Resource private ContainersDao containersDao;

    @Override
    public void execute() {
        try {
            File out = new File(System.getProperty(StringConstants.DB_SCRIPT.getValue()));
            GZIPOutputStream file = new GZIPOutputStream(new FileOutputStream(out));
            settingsManager.flush();
            dumpSettings(file);
            dumpMockedFields(file);
            dumpDescriptors(file);
            dumpDataSources(file);
            dumpContainers(file);
            file.finish();
            file.flush();
            file.close();
        } catch (Exception e) {
            log.error("error during db dump", e);
        }
    }

    private void dumpSettings(OutputStream file) throws IOException {
        Collection<Setting> settings = settingsDao.loadAll();
        append(file, convert(settings, new SettingDumpConverter()));
    }

    private void dumpMockedFields(OutputStream file) throws IOException {
        Collection<MockedField> mockedFields = mockedFieldsDao.loadAll();
        append(file, convert(mockedFields, new MockedFieldDumpConverter()));
    }

    private void dumpContainers(OutputStream file) throws IOException {
        Collection<Container> containers = containersDao.loadAll();
        append(file, convert(containers, new ContainerDumpConverter()));
    }

    private void dumpDescriptors(OutputStream file) throws IOException {
        List<WebApplicationDescriptor> descriptors = webApplicationDescriptorDao.loadAll();
        append(file, convert(descriptors, new DescriptorDumpConverter()));
        append(file, convert(collect(forEach(descriptors, WebApplicationDescriptor.class).getElements()),
                             new DescriptorElementDumpConverter()));
    }

    private void dumpDataSources(OutputStream file) throws IOException {
        List<JndiDataSource> dataSources = jndiDataSourcesDao.loadAll();
        append(file, convert(dataSources, new JndiDataSourceDumpConverter()));
    }

    private void append(OutputStream file, String text) throws IOException {
        if (StringUtils.isNotBlank(text)) {
            file.write(text.getBytes());
            file.write(NEWLINE);
        }
    }

    private void append(OutputStream file, List<String> rows) throws IOException {
        for (String s : rows) {
            append(file, s);
        }
    }
}
