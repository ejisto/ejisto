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

package com.ejisto.core.jetty;

import org.eclipse.jetty.webapp.WebAppContext;

import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;

public class WebAppContextRepository {

    private Map<String, WebAppContext> contextMap = new TreeMap<String, WebAppContext>();

    public void registerWebAppContext(WebAppContext context) {
        contextMap.put(context.getContextPath(), context);
    }

    public void unregisterWebAppContext(WebAppContext context) {
        contextMap.remove(context.getContextPath());
    }

    public WebAppContext getWebAppContext(String contextPath) {
        return contextMap.get(contextPath);
    }

    public Collection<WebAppContext> getAllContexts() {
        return contextMap.values();
    }

    public boolean containsWebAppContext(String contextPath) {
        return contextMap.containsKey(contextPath);
    }

}
