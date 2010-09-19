/*******************************************************************************
 * Copyright 2010 Celestino Bellone
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package com.ejisto.services.shutdown;

import static com.ejisto.util.IOUtils.writeFile;

import java.util.Collection;

import javax.annotation.Resource;

import org.apache.log4j.Logger;

import com.ejisto.constants.StringConstants;
import com.ejisto.modules.conf.SettingsManager;
import com.ejisto.modules.dao.MockedFieldsDao;
import com.ejisto.modules.dao.SettingsDao;
import com.ejisto.modules.dao.entities.MockedField;
import com.ejisto.modules.dao.entities.Setting;

public class DatabaseDump extends BaseShutdownService {

    private static final Logger logger = Logger.getLogger(DatabaseDump.class);
    private static final String INSERT_SETTING = "INSERT INTO SETTINGS VALUES ('%s','%s');";
    private static final String INSERT_FIELD = "INSERT INTO MOCKEDFIELDS(CONTEXTPATH,CLASSNAME,FIELDNAME,FIELDTYPE,FIELDVALUE) VALUES('%s','%s','%s','%s','%s');";
    private static final String NEWLINE = "\n";

    @Resource
    private MockedFieldsDao mockedFieldsDao;
    @Resource
    private SettingsDao settingsDao;
    @Resource
    private SettingsManager settingsManager;

    @Override
    public void execute() {
        try {
            settingsManager.flush();
            StringBuilder file = new StringBuilder();
            Collection<Setting> settings = settingsDao.loadAll();
            for (Setting setting : settings) {
                file.append(String.format(INSERT_SETTING, setting.getKey(), escape(setting.getValue()))).append(NEWLINE);
            }
            Collection<MockedField> mockedFields = mockedFieldsDao.loadAll();
            for (MockedField field : mockedFields) {
                file.append(
                        String.format(INSERT_FIELD, field.getContextPath(), field.getClassName(), field.getFieldName(), field.getFieldType(),
                                escape(field.getFieldValue()))).append(NEWLINE);
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
