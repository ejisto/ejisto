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

package com.ejisto.modules.dao.remote;

import com.ejisto.modules.dao.entities.Setting;
import com.ejisto.modules.web.util.JSONUtil;
import com.fasterxml.jackson.core.type.TypeReference;

import java.util.Collection;
import java.util.List;

import static com.ejisto.constants.StringConstants.CTX_GET_SETTINGS;

/**
 * Created by IntelliJ IDEA.
 * User: celestino
 * Date: 7/4/12
 * Time: 12:27 PM
 */
public class SettingsDao extends BaseRemoteDao implements com.ejisto.modules.dao.SettingsDao {

    @Override
    public Collection<Setting> loadAll() {
        return JSONUtil.decode(remoteCall(encodeRequest("loadAll"), CTX_GET_SETTINGS.getValue()),
                               new TypeReference<List<Setting>>() {
                               });
    }

    @Override
    public Setting getSetting(String key) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public boolean insertSettings(Collection<Setting> settings) {
        throw new UnsupportedOperationException("Remote dao is read-only");
    }

    @Override
    public boolean insertSetting(Setting setting) {
        throw new UnsupportedOperationException("Remote dao is read-only");
    }

    @Override
    public boolean clearSettings(Collection<Setting> settings) {
        throw new UnsupportedOperationException("Remote dao is read-only");
    }
}
