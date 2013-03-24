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

package com.ejisto.modules.dao.db;

import org.mapdb.DB;

import java.util.Map;
import java.util.NavigableSet;
import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: celestino
 * Date: 3/22/13
 * Time: 6:40 PM
 */
public final class DatabaseAccessor {

    private final DB db;

    public DatabaseAccessor(DB db) {
        this.db = db;
    }

    public <K, V> Map<K, V> getHashMap(String name) {
        return db.getHashMap(name);
    }

    public <V> NavigableSet<V> getTreeSet(String name) {
        return db.getTreeSet(name);
    }

    public <V> Set<V> getHashSet(String name) {
        return db.getHashSet(name);
    }

    DB getDb() {
        return db;
    }

}
