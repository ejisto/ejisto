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

import com.ejisto.modules.dao.db.transaction.DefaultTransaction;
import com.ejisto.modules.dao.db.transaction.ReadOnlyTransaction;
import org.mapdb.DB;

import java.lang.ref.WeakReference;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Stores the current transaction
 * <p/>
 * User: celestino
 * Date: 3/15/13
 * Time: 4:47 PM
 */
public final class TransactionRegistry {

    static final Transaction INACTIVE = new Transaction() {
        @Override
        public DatabaseAccessor getDatabaseAccessor() {
            return null;
        }

        @Override
        public String getId() {
            return null;
        }

        @Override
        public boolean commit() {
            return false;
        }

        @Override
        public boolean rollback() {
            return false;
        }

        @Override
        public boolean isActive() {
            return false;
        }
    };
    private static final ConcurrentMap<String, WeakReference<Transaction>> REGISTERED_TRANSACTIONS = new ConcurrentHashMap<>();
    private static final ThreadLocal<String> ACTIVE_TRANSACTION_ID = new ThreadLocal<>();

    private TransactionRegistry() {
    }

    static Transaction getRegisteredTransaction(String id) {
        WeakReference<Transaction> reference = REGISTERED_TRANSACTIONS.get(id);
        if (reference != null) {
            Transaction transaction = reference.get();
            if (transaction != null) {
                ACTIVE_TRANSACTION_ID.set(transaction.getId());
                return new ReadOnlyTransaction(transaction);
            }
        }
        ACTIVE_TRANSACTION_ID.remove();
        return INACTIVE;
    }

    static Transaction create(DB db) {
        if (ACTIVE_TRANSACTION_ID.get() != null) {
            throw new IllegalStateException("There is another running transaction");
        }
        Transaction transaction = new DefaultTransaction(db);
        String id = transaction.getId();
        REGISTERED_TRANSACTIONS.put(id, new WeakReference<>(transaction));
        ACTIVE_TRANSACTION_ID.set(id);
        return transaction;
    }

    static void remove(Transaction tx) {
        if (tx instanceof ReadOnlyTransaction) {
            return;
        }
        if (tx.getId().equals(ACTIVE_TRANSACTION_ID.get())) {
            ACTIVE_TRANSACTION_ID.remove();
        }
        REGISTERED_TRANSACTIONS.remove(tx.getId());
    }

    public static String getActiveTransactionId() {
        return ACTIVE_TRANSACTION_ID.get();
    }
}
