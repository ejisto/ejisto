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
import com.ejisto.modules.dao.entities.Container;
import com.ejisto.modules.dao.exception.UniqueConstraintViolated;

import java.util.*;
import java.util.concurrent.Callable;

/**
 * Created by IntelliJ IDEA.
 * User: celestino
 * Date: 3/6/11
 * Time: 8:54 AM
 */
public class LocalContainersDao extends BaseLocalDao {

    public LocalContainersDao(EmbeddedDatabaseManager database) {
        super(database);
    }

    public List<Container> loadAll() {
        return new ArrayList<>(loadAllRegisteredContainers().values());
    }

    public Container load(String id) {
        return loadAllRegisteredContainers().get(id);
    }

    public boolean insert(final Container container) {
        transactionalOperation(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                if (load(container.getId()) != null) {
                    throw new UniqueConstraintViolated("Container.id cannot be '" + container.getId() + "'");
                }
                loadAllRegisteredContainers().put(container.getKey(), container);
                return null;
            }
        });
        return true;
    }

    public boolean delete(final Container container) {
        transactionalOperation(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                loadAllRegisteredContainers().remove(container.getKey());
                return null;
            }
        });
        return true;
    }

    private Map<String, Container> loadAllRegisteredContainers() {
        return getDatabase().getContainers().orElseThrow(IllegalStateException::new);
    }

}
