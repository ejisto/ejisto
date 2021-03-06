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

package com.ejisto.event.def;

import com.ejisto.constants.StringConstants;

public class ShutdownRequest extends BaseApplicationEvent {

    private static final long serialVersionUID = 5574899567022807174L;

    public ShutdownRequest(Object source) {
        super(source);
    }

    @Override
    public String getDescription() {
        return "System shutdown requested";
    }

    @Override
    public String getKey() {
        return StringConstants.SHUTDOWN.getValue();
    }

    @Override
    protected String getEventDescriptionValue() {
        return getDescription();
    }

    @Override
    public String getIconKey() {
        return "exit.icon";
    }
}
