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

package com.ejisto.modules.vertx.handler.internal;

import com.ejisto.modules.repository.CustomObjectFactoryRepository;
import com.ejisto.modules.vertx.handler.ContextHandler;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.http.RouteMatcher;

import static com.ejisto.constants.StringConstants.CTX_GET_CUSTOM_OBJECT_FACTORY;
import static com.ejisto.modules.vertx.handler.Boilerplate.writeOutputAsJSON;

/**
 * Created by IntelliJ IDEA.
 * User: celestino
 * Date: 7/18/14
 */
public class CustomObjectFactoryHandler implements ContextHandler {

    private final CustomObjectFactoryRepository customObjectFactoryRepository;

    public CustomObjectFactoryHandler(CustomObjectFactoryRepository customObjectFactoryRepository) {
        this.customObjectFactoryRepository = customObjectFactoryRepository;
    }

    @Override
    public void addRoutes(RouteMatcher routeMatcher) {
        routeMatcher.get(CTX_GET_CUSTOM_OBJECT_FACTORY.getValue(), this::loadCustomObjectFactories);
    }

    private void loadCustomObjectFactories(HttpServerRequest req) {
        writeOutputAsJSON(customObjectFactoryRepository.getCustomObjectFactories(), req.response());
    }
}
