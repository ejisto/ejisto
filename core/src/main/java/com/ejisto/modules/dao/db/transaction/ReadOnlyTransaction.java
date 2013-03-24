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

package com.ejisto.modules.dao.db.transaction;

import com.ejisto.modules.dao.db.DatabaseAccessor;
import com.ejisto.modules.dao.db.Transaction;

import java.util.Objects;

/**
 * Created by IntelliJ IDEA.
 * User: celestino
 * Date: 3/15/13
 * Time: 4:34 PM
 */
public class ReadOnlyTransaction implements Transaction {

    private final String id;
    private final Transaction original;

    public ReadOnlyTransaction(Transaction original) {
        Objects.requireNonNull(original);
        this.id = original.getId();
        this.original = original;
    }

    @Override
    public DatabaseAccessor getDatabaseAccessor() {
        return original.getDatabaseAccessor();
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public boolean commit() {
        return true;
    }

    @Override
    public boolean rollback() {
        return true;
    }

    @Override
    public boolean isActive() {
        return original.isActive();
    }
}
