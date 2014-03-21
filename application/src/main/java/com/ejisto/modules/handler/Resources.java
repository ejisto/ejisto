/*
 * Ejisto, a powerful developer assistant
 *
 * Copyright (C) 2010-2014 Celestino Bellone
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

package com.ejisto.modules.handler;

import org.vertx.java.core.http.RouteMatcher;

/**
 * Created by IntelliJ IDEA.
 * User: celestino
 * Date: 3/21/14
 * Time: 7:05 PM
 */
public class Resources implements ContextHandler {

    @Override
    public RouteMatcher getRouteMatcher() {
        return Boilerplate.resourcesMatcher();
    }

}
