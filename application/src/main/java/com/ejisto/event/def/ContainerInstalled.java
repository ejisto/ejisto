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
 * Date: 8/8/11
 * Time: 9:40 PM
 */
public class ContainerInstalled extends BaseApplicationEvent {
    private static final long serialVersionUID = 1670481880287700224L;
    private String id;
    private String description;

    public ContainerInstalled(Object source, String id, String description) {
        super(source);
        this.id = id;
        this.description = description;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public String getKey() {
        return id;
    }

    @Override
    public boolean shouldRunOnEDT() {
        return false;
    }

    public String getContainerId() {
        return id;
    }
}
