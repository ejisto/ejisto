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

import com.ejisto.modules.dao.entities.Setting;

import java.util.ArrayList;
import java.util.List;

import static ch.lambdaj.Lambda.*;
import static org.hamcrest.CoreMatchers.equalTo;

public class SettingsDao extends BaseLocalDao implements com.ejisto.modules.dao.SettingsDao {

    @Override
    public List<Setting> loadAll() {
        return new ArrayList<>(getDatabase().getSettings());
    }

    @Override
    public Setting getSetting(String key) {
        return selectFirst(getDatabase().getSettings(), having(on(Setting.class).getKey(), equalTo(key)));
    }

    @Override
    public boolean insertSettings(final List<Setting> settings) {
        boolean success = getDatabase().getSettings().addAll(settings);
        tryToCommit();
        return success;
    }

    @Override
    public boolean clearSettings(final List<Setting> settings) {
        boolean success = getDatabase().getSettings().removeAll(settings);
        tryToCommit();
        return success;
    }
}
