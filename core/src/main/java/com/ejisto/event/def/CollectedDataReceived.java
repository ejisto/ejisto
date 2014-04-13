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

import com.ejisto.modules.recorder.CollectedData;

/**
 * Created by IntelliJ IDEA.
 * User: celestino
 * Date: 10/23/12
 * Time: 8:06 AM
 */
public class CollectedDataReceived extends BaseApplicationEvent {

    private final CollectedData data;

    public CollectedDataReceived(Object source, CollectedData data) {
        super(source);
        this.data = data;
    }

    @Override
    public String getDescription() {
        return "";
    }

    @Override
    public String getKey() {
        return "recordingResultReceived";
    }

    @Override
    protected String getEventDescriptionValue() {
        return "";
    }

    public CollectedData getData() {
        return data;
    }

}
