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

package com.ejisto.services.shutdown;

import com.ejisto.constants.StringConstants;
import com.ejisto.modules.conf.SettingsManager;
import com.ejisto.modules.dao.*;
import com.ejisto.modules.dao.entities.*;
import com.ejisto.util.converter.*;
import lombok.extern.log4j.Log4j;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.List;

import static ch.lambdaj.Lambda.*;
import static com.ejisto.util.IOUtils.writeFile;

@Log4j
public class DatabaseDump extends BaseShutdownService {

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
            writeFile(file.toString().getBytes(), System.getProperty(StringConstants.DB_SCRIPT.getValue()));
        } catch (Exception e) {
            log.error("error during db dump", e);
        }
    }

    private void dumpSettings(StringBuilder file) {
        Collection<Setting> settings = settingsDao.loadAll();
        append(file, convert(settings, new SettingDumpConverter()));
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
        append(file, convert(descriptors, new DescriptorDumpConverter()));
        append(file, convert(collect(forEach(descriptors, WebApplicationDescriptor.class).getElements()),
                             new DescriptorElementDumpConverter()));
    }

    private void dumpDataSources(StringBuilder file) {
        List<JndiDataSource> dataSources = jndiDataSourcesDao.loadAll();
        append(file, convert(dataSources, new JndiDataSourceDumpConverter()));
    }

    private void append(StringBuilder file, String text) {
        if (StringUtils.hasText(text.trim())) file.append(text).append(NEWLINE);
    }

    private void append(StringBuilder file, List<String> rows) {
        for (String s : rows) {
            append(file, s);
        }
    }
}
