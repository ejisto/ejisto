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
 * Date: 11/27/11
 * Time: 7:57 PM
 */
public class ProgressDescriptor {
    public enum ProgressState {
        RUNNING, INDETERMINATE, COMPLETED
    }

    private final int progress;
    private final String message;
    private final ProgressState progressState;

    public ProgressDescriptor(int progress, String message, ProgressState progressState) {
        this.progress = progress;
        this.message = message;
        this.progressState = progressState;
    }

    public ProgressDescriptor(int progress, String message) {
        this(progress, message, ProgressState.RUNNING);
    }

    public int getProgress() {
        return progress;
    }

    public String getMessage() {
        return message;
    }

    public boolean isIndeterminate() {
        return progressState == ProgressState.INDETERMINATE;
    }

    public boolean isTaskCompleted() {
        return progressState == ProgressState.COMPLETED;
    }

    public ProgressState getProgressState() {
        return progressState;
    }
}
