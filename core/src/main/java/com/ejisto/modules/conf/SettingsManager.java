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

package com.ejisto.modules.conf;

import com.ejisto.constants.StringConstants;
import com.ejisto.modules.dao.SettingsDao;
import com.ejisto.modules.dao.entities.Setting;
import com.ejisto.util.ExternalizableService;

import javax.annotation.Resource;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import static ch.lambdaj.Lambda.*;
import static org.hamcrest.Matchers.equalTo;

public class SettingsManager extends ExternalizableService<SettingsDao> {

    @Resource(name = "settings") private Properties settings;
    @Resource private SettingsDao settingsDao;

    public int getIntValue(StringConstants key) {
        return Integer.parseInt(getValue(key));
    }

    public boolean getBooleanValue(StringConstants key) {
        return Boolean.parseBoolean(getValue(key));
    }

    public String getValue(StringConstants key) {
        return getValue(key.getValue());
    }

    public String getValueFromProperties(StringConstants key) {
        return settings.getProperty(key.getValue());
    }

    public String getValue(String key) {
        Setting setting = find(key);
        if (setting != null) {
            return setting.getValue();
        }
        setting = new Setting(key, settings.getProperty(key));
        settingsDao.insertSetting(setting);
        return setting.getValue();
    }

    public void putValue(StringConstants key, Object value) {
        putValue(key.getValue(), String.valueOf(value));
    }

    private void putValue(String key, String value) {
        settingsDao.insertSetting(new Setting(key, value));
    }

    private Setting find(String key) {
        return settingsDao.getSetting(key);
    }

    private com.ejisto.modules.dao.SettingsDao getSettingsDao() {
        checkDao();
        return settingsDao;
    }

    @Override
    protected com.ejisto.modules.dao.SettingsDao getDaoInstance() {
        return settingsDao;
    }

    @Override
    protected void setDaoInstance(com.ejisto.modules.dao.SettingsDao daoInstance) {
        this.settingsDao = daoInstance;
    }

    @Override
    protected SettingsDao newRemoteDaoInstance() {
        return new com.ejisto.modules.dao.remote.SettingsDao();
    }

}
