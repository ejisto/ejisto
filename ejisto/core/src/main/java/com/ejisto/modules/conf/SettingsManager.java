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

package com.ejisto.modules.conf;

import com.ejisto.constants.StringConstants;
import com.ejisto.modules.dao.SettingsDao;
import com.ejisto.modules.dao.entities.Setting;
import com.ejisto.util.ExternalizableService;
import org.springframework.beans.factory.InitializingBean;

import javax.annotation.Resource;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicReference;

import static ch.lambdaj.Lambda.*;
import static org.hamcrest.Matchers.equalTo;

public class SettingsManager extends ExternalizableService<SettingsDao> implements InitializingBean {
    @Resource(name = "settings") private Properties settings;
    @Resource private SettingsDao settingsDao;

    private AtomicReference<List<Setting>> settingsList = new AtomicReference<List<Setting>>();

    public int getIntValue(StringConstants key) {
        return Integer.parseInt(getValue(key));
    }

    public String getValue(StringConstants key) {
        return getValue(key.getValue());
    }

    public String getValueFromProperties(StringConstants key) {
        return settings.getProperty(key.getValue());
    }

    public String getValue(String key) {
        init();
        Setting setting = find(key);
        if (setting != null) return setting.getValue();
        setting = new Setting(key, settings.getProperty(key));
        settingsList.get().add(setting);
        return setting.getValue();
    }

    public void flush() {
        if (getSettingsDao().clearSettings(settingsList.get())) getSettingsDao().insertSettings(settingsList.get());
    }

    public void putValue(StringConstants key, Object value) {
        putValue(key.getValue(), String.valueOf(value));
    }

    private void putValue(String key, String value) {
        Setting setting = find(key);
        if (setting != null) {
            setting.setValue(value);
        } else {
            setting = new Setting(key, value);
            settingsList.get().add(setting);
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
    }

    private void init() {
        if (settingsList.get() == null)
            settingsList.compareAndSet(null, getSettingsDao().loadAll());
    }

    private Setting find(String key) {
        return selectFirst(settingsList.get(), having(on(Setting.class).getKey(), equalTo(key)));
    }

    private SettingsDao getSettingsDao() {
        checkDao();
        return settingsDao;
    }

    @Override
    protected SettingsDao getDaoInstance() {
        return settingsDao;
    }

    @Override
    protected void setDaoInstance(SettingsDao daoInstance) {
        this.settingsDao = daoInstance;
    }

    @Override
    protected Class<SettingsDao> getDaoClass() {
        return SettingsDao.class;
    }
}
