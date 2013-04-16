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

package com.ejisto.modules.executor;

/**
 * Created by IntelliJ IDEA.
 * User: celestino
 * Date: 4/2/11
 * Time: 7:47 PM
 */
public class TaskDescriptor {
    private final String uuid;
    private final String description;
    private final ExecutionState executionState;

    public TaskDescriptor(String uuid, String description, ExecutionState executionState) {
        this.description = description;
        this.executionState = executionState;
        this.uuid = uuid;
    }

    public String getDescription() {
        return description;
    }

    public ExecutionState getExecutionState() {
        return executionState;
    }

    public String getUuid() {
        return uuid;
    }
}
