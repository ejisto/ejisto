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

import org.springframework.context.ApplicationEvent;

public abstract class BaseApplicationEvent extends ApplicationEvent {

    private static final long serialVersionUID = -2616190172593844455L;

    BaseApplicationEvent(Object source) {
        super(source);
    }

    public abstract String getDescription();

    public String getIconKey() {
        return "";
    }

    public abstract String getKey();

    @Override
    public String toString() {
        return getDescription();
    }
}
