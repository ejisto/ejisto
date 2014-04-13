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
 * Date: 2/21/12
 * Time: 6:28 PM
 */
public class ContainerStatusChanged extends BaseApplicationEvent {

    private boolean started;
    private String containerId;

    public ContainerStatusChanged(Object source, String containerId, boolean started) {
        super(source);
        this.started = started;
        this.containerId = containerId;
    }

    @Override
    public String getDescription() {
        return "container " + containerId + (started ? " started" : " stopped");
    }

    @Override
    public String getKey() {
        return started ? "container.started" : "container.stopped";
    }

    @Override
    protected String getEventDescriptionValue() {
        return getContainerId();
    }

    public boolean isStarted() {
        return started;
    }

    public String getContainerId() {
        return containerId;
    }
}
