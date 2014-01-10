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

package com.ejisto.modules.dao.local;

import com.ejisto.modules.dao.SettingsDao;
import com.ejisto.modules.dao.db.EmbeddedDatabaseManager;
import com.ejisto.modules.dao.entities.Setting;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class LocalSettingsDao extends BaseLocalDao implements SettingsDao {

    public LocalSettingsDao(EmbeddedDatabaseManager database) {
        super(database);
    }

    @Override
    public Collection<Setting> loadAll() {
        return new HashSet<>(loadAllSettings().values());
    }

    @Override
    public Setting getSetting(String key) {
        return loadAllSettings().get(key);
    }

    @Override
    public boolean insertSettings(final Collection<Setting> settings) {
        transactionalOperation(() -> {
            loadAllSettings().putAll(settings.stream().collect(HashMap::new, (acc, x) -> acc.put(x.getKey(), x), Map::putAll));
            return null;
        });
        return true;
    }

    @Override
    public boolean insertSetting(final Setting setting) {
        return transactionalOperation(() -> {
            loadAllSettings().put(setting.getKey(), setting);
            return Boolean.TRUE;
        });
    }

    @Override
    public boolean clearSettings(final Collection<Setting> settings) {
        transactionalOperation(() -> {
            final Map<String, Setting> existing = loadAllSettings();
            settings.stream().forEach(s -> existing.remove(s.getKey()));
            return null;
        });
        return true;
    }

    private Map<String, Setting> loadAllSettings() {
        return getDatabase().getSettings().orElseThrow(IllegalStateException::new);
    }
}