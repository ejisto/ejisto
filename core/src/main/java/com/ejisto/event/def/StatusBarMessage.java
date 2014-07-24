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

public class StatusBarMessage extends BaseApplicationEvent {
    private static final long serialVersionUID = -8894492901808590505L;
    private final String message;
    private final boolean error;

    public StatusBarMessage(Object source, String message, boolean error) {
        super(source);
        this.message = message;
        this.error = error;
    }

    @Override
    public String getDescription() {
        return null;
    }

    @Override
    public String getKey() {
        return null;
    }

    @Override
    protected String getEventDescriptionValue() {
        return getMessage();
    }

    public String getMessage() {
        return message;
    }

    public boolean isError() {
        return error;
    }

}
