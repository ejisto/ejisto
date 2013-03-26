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

import com.ejisto.modules.dao.SettingsDao;
import com.ejisto.modules.dao.entities.Setting;
import com.ejisto.modules.web.util.JSONUtil;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import lombok.extern.log4j.Log4j;

import javax.annotation.Resource;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;

/**
 * Created by IntelliJ IDEA.
 * User: celestino
 * Date: 7/4/12
 * Time: 12:29 PM
 */
@Log4j
public class SettingsHandler implements HttpHandler {

    @Resource private SettingsDao settingsDao;

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        try (OutputStream os = httpExchange.getResponseBody()) {
            Collection<Setting> factories = settingsDao.loadAll();
            String response = JSONUtil.encode(factories);
            httpExchange.sendResponseHeaders(200, response.length());
            os.write(response.getBytes());
            os.close();
        } catch (Exception e) {
            log.error("error during settings handling", e);
        }
    }
}
