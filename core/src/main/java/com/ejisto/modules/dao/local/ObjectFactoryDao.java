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

package com.ejisto.modules.dao.local;

import com.ejisto.modules.dao.db.EmbeddedDatabaseManager;
import com.ejisto.modules.dao.entities.RegisteredObjectFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * Created by IntelliJ IDEA.
 * User: celestino
 * Date: 7/31/11
 * Time: 6:46 PM
 */
public class ObjectFactoryDao extends BaseLocalDao implements com.ejisto.modules.dao.ObjectFactoryDao {

    public ObjectFactoryDao(EmbeddedDatabaseManager database) {
        super(database);
    }

    @Override
    public List<RegisteredObjectFactory> loadAll() {
        return transactionalOperation(new Callable<List<RegisteredObjectFactory>>() {
            @Override
            public List<RegisteredObjectFactory> call() throws Exception {
                return new ArrayList<>(getDatabase().getRegisteredObjectFactories().values());
            }
        });
    }

    @Override
    public void insert(final RegisteredObjectFactory registeredObjectFactory) {
        transactionalOperation(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                getDatabase().getRegisteredObjectFactories().put(registeredObjectFactory.getKey(),
                                                                 registeredObjectFactory);
                return null;
            }
        });
    }
}
