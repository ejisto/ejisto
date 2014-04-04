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

package com.ejisto.modules.handler.service;

import com.ejisto.modules.gui.components.helper.FieldsEditorContext;
import com.ejisto.modules.handler.ContextHandler;
import com.ejisto.modules.repository.MockedFieldsRepository;
import com.ejisto.modules.web.util.JSONUtil;
import com.ejisto.util.collector.FieldNode;
import com.ejisto.util.collector.MockedFieldCollector;
import org.vertx.java.core.http.HttpHeaders;
import org.vertx.java.core.http.RouteMatcher;

/**
 * Created by IntelliJ IDEA.
 * User: celestino
 * Date: 3/28/14
 * Time: 6:27 PM
 */
public class FieldService implements ContextHandler {

    private final MockedFieldsRepository mockedFieldsRepository;

    public FieldService(MockedFieldsRepository mockedFieldsRepository) {
        this.mockedFieldsRepository = mockedFieldsRepository;
    }

    @Override
    public void addRoutes(RouteMatcher routeMatcher) {
        routeMatcher.get("/fields/grouped", request -> {
            final FieldNode node = mockedFieldsRepository.loadAll()
                    .parallelStream()
                    .filter(FieldsEditorContext.MAIN_WINDOW::isAdmitted)
                    .collect(new MockedFieldCollector());
            String result = JSONUtil.encode(node);
            request.response()
                    .putHeader(HttpHeaders.CONTENT_LENGTH, String.valueOf(result.length()))
                    .putHeader(HttpHeaders.CONTENT_TYPE, "application/json")
                    .write(result)
                    .end();
        });
    }

}
