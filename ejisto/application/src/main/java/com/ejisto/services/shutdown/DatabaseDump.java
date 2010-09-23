/*
 * Ejisto, a powerful developer assistant
 *
 * Copyright (C) 2010  Celestino Bellone
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
import com.ejisto.modules.dao.MockedFieldsDao;
import com.ejisto.modules.dao.SettingsDao;
import com.ejisto.modules.dao.WebApplicationDescriptorDao;
import com.ejisto.modules.dao.entities.MockedField;
import com.ejisto.modules.dao.entities.Setting;
import com.ejisto.modules.dao.entities.WebApplicationDescriptor;
import com.ejisto.modules.dao.entities.WebApplicationDescriptorElement;
import org.apache.log4j.Logger;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.List;

import static com.ejisto.util.IOUtils.writeFile;
import static java.lang.String.format;

public class DatabaseDump extends BaseShutdownService {

    private static final Logger logger = Logger.getLogger(DatabaseDump.class);
    private static final String INSERT_SETTING = "INSERT INTO SETTINGS VALUES ('%s','%s');";
    private static final String INSERT_FIELD   = "INSERT INTO MOCKEDFIELDS(CONTEXTPATH,CLASSNAME,FIELDNAME,FIELDTYPE,FIELDVALUE) VALUES('%s','%s','%s','%s','%s');";
    private static final String INSERT_CONTEXT = "INSERT INTO WEBAPPLICATIONDESCRIPTOR(CONTEXTPATH,INSTALLATIONPATH) VALUES('%s','%s');";
    private static final String INSERT_ELEMENT = "INSERT INTO WEBAPPLICATIONDESCRIPTORELEMENT(CONTEXTPATH,PATH,KIND) VALUES('%s','%s','%s');";
    private static final String NEWLINE = "\n";

    @Resource
    private MockedFieldsDao mockedFieldsDao;
    @Resource
    private SettingsDao settingsDao;
    @Resource
    private SettingsManager settingsManager;
    @Resource
    private WebApplicationDescriptorDao webApplicationDescriptorDao;

    @Override
    public void execute() {
        try {
            settingsManager.flush();
            StringBuilder file = new StringBuilder();
            Collection<Setting> settings = settingsDao.loadAll();
            for (Setting setting : settings) {
                file.append(format(INSERT_SETTING, setting.getKey(), escape(setting.getValue()))).append(NEWLINE);
            }
            Collection<MockedField> mockedFields = mockedFieldsDao.loadAll();
            for (MockedField field : mockedFields) {
                file.append(
                        format(INSERT_FIELD, field.getContextPath(), field.getClassName(), field.getFieldName(), field.getFieldType(),
                                escape(field.getFieldValue()))).append(NEWLINE);
            }
            List<WebApplicationDescriptor> descriptors = webApplicationDescriptorDao.loadAll();
            for (WebApplicationDescriptor descriptor : descriptors) {
                file.append(format(INSERT_CONTEXT, descriptor.getContextPath(), descriptor.getInstallationPath())).append(NEWLINE);
                for (WebApplicationDescriptorElement element : descriptor.getElements()) 
                    file.append(format(INSERT_ELEMENT, element.getContextPath(), element.getPath(), element.getKind())).append(NEWLINE);
            }
            writeFile(file.toString().getBytes(), System.getProperty(StringConstants.DERBY_SCRIPT.getValue()));
        } catch (Exception e) {
            logger.error("error during db dump", e);
        }
    }
    
    private String escape(String in) {
        return in.replaceAll("'", "''");
    }

}
