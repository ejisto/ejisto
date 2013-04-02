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

package com.ejisto.modules.repository;

import com.ejisto.constants.StringConstants;
import com.ejisto.modules.conf.SettingsManager;

import javax.annotation.Resource;

/**
 * Created by IntelliJ IDEA.
 * User: celestino
 * Date: Dec 4, 2010
 * Time: 6:07:09 PM
 */
public final class SettingsRepository {

    private final SettingsManager settingsManager;

    private SettingsRepository(SettingsManager settingsManager) {
        this.settingsManager = settingsManager;
    }

    public String getSettingValue(StringConstants key) {
        return settingsManager.getValue(key);
    }

    public int getSettingIntValue(StringConstants key) {
        return Integer.parseInt(getSettingValue(key));
    }

    public void putSettingValue(StringConstants key, Object value) {
        settingsManager.putValue(key, value);
    }
}
