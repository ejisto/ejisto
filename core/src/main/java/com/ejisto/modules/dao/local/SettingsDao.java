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

import ch.lambdaj.function.convert.Converter;
import com.ejisto.modules.dao.entities.Setting;
import com.ejisto.util.converter.EntityToKey;

import java.util.*;

import static ch.lambdaj.Lambda.map;

public class SettingsDao extends BaseLocalDao implements com.ejisto.modules.dao.SettingsDao {

    private static final Converter<Setting, String> KEY_EXTRACTOR = new EntityToKey<>();

    @Override
    public Collection<Setting> loadAll() {
        return new HashSet<>(getDatabase().getSettings().values());
    }

    @Override
    public Setting getSetting(String key) {
        return getDatabase().getSettings().get(key);
    }

    @Override
    public boolean insertSettings(final Collection<Setting> settings) {
        getDatabase().getSettings().putAll(map(settings, KEY_EXTRACTOR));
        tryToCommit();
        return true;
    }

    @Override
    public boolean clearSettings(final Collection<Setting> settings) {
        for (Setting setting : settings) {
            getDatabase().getSettings().remove(setting.getKey());
        }
        tryToCommit();
        return true;
    }
}
