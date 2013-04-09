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

package com.ejisto.modules.web.handler;

import com.ejisto.constants.StringConstants;
import com.ejisto.modules.dao.ObjectFactoryDao;
import com.ejisto.modules.dao.entities.RegisteredObjectFactory;
import com.ejisto.modules.web.RemoteRequestHandler;
import com.ejisto.modules.web.util.JSONUtil;
import com.sun.net.httpserver.HttpExchange;
import lombok.extern.log4j.Log4j;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import static com.ejisto.constants.StringConstants.CTX_GET_OBJECT_FACTORY;

/**
 * Created by IntelliJ IDEA.
 * User: celestino
 * Date: 7/4/12
 * Time: 11:09 AM
 */
@Log4j
public class ObjectFactoryHandler implements RemoteRequestHandler {

    private final ObjectFactoryDao objectFactoryDao;

    public ObjectFactoryHandler(ObjectFactoryDao objectFactoryDao) {
        this.objectFactoryDao = objectFactoryDao;
    }

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        try (OutputStream os = httpExchange.getResponseBody()) {
            List<RegisteredObjectFactory> factories = objectFactoryDao.loadAll();
            String response = JSONUtil.encode(factories);
            httpExchange.sendResponseHeaders(200, response.length());
            os.write(response.getBytes());
        } catch (Exception e) {
            log.error("error during objectFactory handling", e);
        }
    }

    @Override
    public String getContextPath() {
        return CTX_GET_OBJECT_FACTORY.getValue();
    }
}
