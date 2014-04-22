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

import com.ejisto.util.WebAppContextStatusCommand;

public class WebAppContextStatusChanged extends BaseApplicationEvent {
    private static final long serialVersionUID = 1350522622740164683L;

    private final WebAppContextStatusCommand command;
    private final String contextPath;

    public WebAppContextStatusChanged(Object source, WebAppContextStatusCommand command, String contextPath) {
        super(source);
        this.command = command;
        this.contextPath = contextPath;
    }

    @Override
    public String getDescription() {
        return "";
    }

    @Override
    public String getKey() {
        return null;
    }

    @Override
    protected String getEventDescriptionValue() {
        return getContextPath();
    }

    public WebAppContextStatusCommand getCommand() {
        return command;
    }

    public String getContextPath() {
        return contextPath;
    }

}
