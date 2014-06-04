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

package com.ejisto.modules.vertx.handler.service;

import com.ejisto.modules.dao.entities.WebApplicationDescriptor;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Created by IntelliJ IDEA.
 * User: celestino
 * Date: 6/4/14
 * Time: 7:28 AM
 */
public class ApplicationInstallerRegistry {

    private static final ConcurrentMap<String, WebApplicationDescriptor> REGISTRY = new ConcurrentHashMap<>();

    private ApplicationInstallerRegistry() {

    }

    public static WebApplicationDescriptor putDescriptorIfAbsent(String key, WebApplicationDescriptor value) {
        return REGISTRY.putIfAbsent(key, value);
    }

    public static Optional<WebApplicationDescriptor> getDescriptor(String key) {
        return Optional.ofNullable(REGISTRY.get(key));
    }

    public static boolean isPresent(String key) {
        return REGISTRY.containsKey(key);
    }
}
