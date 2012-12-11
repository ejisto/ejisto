/*
 * Ejisto, a powerful developer assistant
 *
 * Copyright (C) 2010-2012  Celestino Bellone
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

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpSession;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Created by IntelliJ IDEA.
 * User: celestino
 * Date: 8/24/12
 * Time: 5:30 PM
 */
public class RequestWrapper extends HttpServletRequestWrapper {

    private final AtomicReference<SessionWrapper> session = new AtomicReference<SessionWrapper>();
    private final DataCollector dataCollector;

    public RequestWrapper(HttpServletRequest source, DataCollector dataCollector) {
        super(source);
        this.dataCollector = dataCollector;
    }

    @Override
    public void setAttribute(String name, Object o) {
        dataCollector.putRequestAttribute(name, o);
        super.setAttribute(name, o);
    }


    @Override
    public void removeAttribute(String name) {
        super.removeAttribute(name);
    }

    @Override
    public RequestDispatcher getRequestDispatcher(String path) {
        return new RequestDispatcherWrapper(super.getRequestDispatcher(path), path, dataCollector);
    }

    @Override
    public HttpSession getSession(boolean create) {
        HttpSession httpSession = super.getSession(false);
        if (httpSession == null) {
            SessionWrapper newSession = null;
            if (create) {
                newSession = new SessionWrapper(super.getSession(true), dataCollector);
            }
            SessionWrapper existing = session.get();
            int counter = 0;
            while (!session.compareAndSet(existing, newSession)) {
                existing = session.get();
                if (counter++ > 4) {
                    throw new IllegalStateException("unable to set current state. Aborted");
                }
            }
        }
        return session.get();
    }
}


