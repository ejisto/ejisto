/*
 * Ejisto, a powerful developer assistant
 *
 * Copyright (C) 2010-2011  Celestino Bellone
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

package com.ejisto.event.def;

import com.ejisto.constants.StringConstants;

public class LoadWebApplication extends BaseApplicationEvent {
    private static final long serialVersionUID = 871817827988790201L;
    private boolean loadStored;

    public LoadWebApplication(Object source) {
        super(source);
    }

    @Override
    public String getDescription() {
        return "Load new application";
    }

    @Override
    public String getKey() {
        return StringConstants.LOAD_WEB_APP.getValue();
    }

    public boolean loadStored() {
        return this.loadStored;
    }

    public void setLoadStored(boolean loadStored) {
        this.loadStored = loadStored;
    }

}
