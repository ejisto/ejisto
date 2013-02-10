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

package com.ejisto.modules.dao.entities;

import lombok.Data;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by IntelliJ IDEA.
 * User: celestino
 * Date: 3/6/11
 * Time: 8:44 AM
 */
@Data
public class Container {
    private String id;
    private String cargoId;
    private String homeDir;
    private String description;
    private transient int port = 8080;
    private transient final AtomicBoolean running = new AtomicBoolean(false);
    private transient final boolean standalone;

    public Container() {
        this(false);
    }

    public Container(boolean standalone) {
        this.standalone = standalone;
    }

    public boolean isRunning() {
        return running.get();
    }

    @Deprecated
    public AtomicBoolean getRunning() {
        throw new UnsupportedOperationException(
                "you can't modify 'running' field, use isRunning() or setRunningState()");
    }

    public boolean setRunningState(boolean runningState) {
        return running.compareAndSet(!runningState, runningState);
    }


}
