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

/**
 * Created by IntelliJ IDEA.
 * User: celestino
 * Date: 3/6/11
 * Time: 11:38 AM
 */
public class InstallContainer extends BaseApplicationEvent {

    private String containerId;
    private boolean start;

    public InstallContainer(Object source, String containerId, boolean start) {
        super(source);
        this.containerId = containerId;
        this.start = start;
    }

    public boolean isStart() {
        return start;
    }

    public String getContainerId() {
        return containerId;
    }

    @Override
    public String getDescription() {
        return containerId;
    }

    @Override
    public String getKey() {
        return null;
    }
}
