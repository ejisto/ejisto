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

package com.ejisto.modules.dao.entities;

import com.ejisto.constants.StringConstants;


public class Setting {

    private String key;
    private String value;
    private transient StringConstants humanReadableKey;
    
    public Setting() {
    }
    
    public Setting(String key, String value) {
		this.key = key;
		this.value = value;
	}

	public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

	public void setHumanReadableKey(StringConstants humanReadableKey) {
		this.humanReadableKey = humanReadableKey;
	}

	public StringConstants getHumanReadableKey() {
		return humanReadableKey;
	}
}