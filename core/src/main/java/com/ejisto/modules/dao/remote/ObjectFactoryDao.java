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

package com.ejisto.modules.dao.remote;

import com.ejisto.modules.dao.entities.RegisteredObjectFactory;
import com.ejisto.modules.web.util.JSONUtil;
import com.fasterxml.jackson.core.type.TypeReference;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: celestino
 * Date: 7/4/12
 * Time: 11:38 AM
 */
public class ObjectFactoryDao extends BaseRemoteDao implements com.ejisto.modules.dao.ObjectFactoryDao {

    @Override
    public List<RegisteredObjectFactory> loadAll() {
        return JSONUtil.decode(remoteCall(encodeRequest("loadAll"), "/getObjectFactory"),
                               new TypeReference<List<RegisteredObjectFactory>>() {
                               });
    }

    @Override
    public void insert(RegisteredObjectFactory registeredObjectFactory) {
        throw new UnsupportedOperationException("Remote dao is read-only");
    }
}
