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
 * Date: 8/9/11
 * Time: 10:47 PM
 */
public class ApplicationDeployed extends BaseApplicationEvent {
    private static final long serialVersionUID = -1430019877487367259L;
    private String context;
    private String containerId;

    public ApplicationDeployed(Object source, String context, String containerId) {
        super(source);
        this.context = context;
        this.containerId = containerId;
    }

    @Override
    public String getDescription() {
        return "deployed " + context + " on " + containerId;
    }

    @Override
    public String getKey() {
        return context;
    }

    public String getContext() {
        return context;
    }

    public String getContainerId() {
        return containerId;
    }

    @Override
    public boolean isRunOnEDT() {
        return true;
    }
}
