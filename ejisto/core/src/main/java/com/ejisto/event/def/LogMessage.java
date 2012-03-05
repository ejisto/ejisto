/*
 * Ejisto, a powerful developer assistant
 *
 * Copyright (C) 2010-2012  Celestino Bellone
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

public class LogMessage extends BaseApplicationEvent {
    private static final long serialVersionUID = 4174138101995544515L;
    private String message;
    private String containerId;

    public LogMessage(Object source, String message, String containerId) {
        super(source);
        this.message = message;
        this.containerId = containerId;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public String getDescription() {
        return "log message";
    }

    @Override
    public String getKey() {
        return null;
    }

    @Override
    public boolean isRunOnEDT() {
        return true;
    }

    public String getContainerId() {
        return containerId;
    }
}
