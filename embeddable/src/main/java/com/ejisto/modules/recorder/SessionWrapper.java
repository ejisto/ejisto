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

package com.ejisto.modules.recorder;

import lombok.Delegate;

import javax.servlet.http.HttpSession;

/**
 * Created by IntelliJ IDEA.
 * User: celestino
 * Date: 8/27/12
 * Time: 6:41 PM
 */
public class SessionWrapper implements HttpSession {

    @Delegate(excludes = SessionRecorder.class) private final HttpSession source;
    private final DataCollector collector;

    public SessionWrapper(HttpSession source, DataCollector collector) {
        this.source = source;
        this.collector = collector;
    }

    @Override
    public void setAttribute(String name, Object value) {
        collector.putSessionAttribute(name, value);
        source.setAttribute(name, value);
    }

    private interface SessionRecorder {
        void setAttribute(String name, Object value);
    }

}
