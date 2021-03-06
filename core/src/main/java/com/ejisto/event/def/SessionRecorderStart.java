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

/**
 * Created by IntelliJ IDEA.
 * User: celestino
 * Date: 9/14/12
 * Time: 7:59 AM
 */
public class SessionRecorderStart extends BaseApplicationEvent {

    private String webApplicationContextPath;

    public SessionRecorderStart(Object source, String webApplicationContextPath) {
        super(source);
        this.webApplicationContextPath = webApplicationContextPath;
    }

    @Override
    public String getDescription() {
        return "Temporary Server start";
    }

    @Override
    public String getKey() {
        return "temp.server.start";
    }

    @Override
    protected String getEventDescriptionValue() {
        return null;
    }

    public String getWebApplicationContextPath() {
        return webApplicationContextPath;
    }
}
