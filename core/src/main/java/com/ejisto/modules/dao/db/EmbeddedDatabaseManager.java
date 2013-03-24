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

import com.ejisto.constants.StringConstants;
import com.ejisto.modules.dao.db.util.MockedFieldContainer;
import com.ejisto.modules.dao.db.util.MockedFieldContainerSorter;
import com.ejisto.modules.dao.db.util.serializer.*;
import com.ejisto.modules.dao.entities.*;
import lombok.extern.log4j.Log4j;
import org.mapdb.Atomic;
import org.mapdb.DB;
import org.mapdb.DBMaker;

import java.io.File;
import java.util.Collection;
import java.util.Map;
import java.util.NavigableSet;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

@Log4j
public class EmbeddedDatabaseManager {

    private static final String SETTINGS = "settings";
    private static final String CONTAINERS = "containers";
    private static final String CUSTOM_OBJECT_FACTORIES = "customObjectFactories";
    private static final String REGISTERED_OBJECT_FACTORIES = "registeredObjectFactories";
    private static final String WEB_APPLICATION_DESCRIPTORS = "webApplicationDescriptors";
    private static final String REGISTERED_CONTEXT_PATHS = "registeredContextPaths";
    private static final String MOCKED_FIELDS_SEQ = "mockedFieldsSeq";
    private static final String STARTUP_COUNTER = "startupCounter";
    private static final ReentrantLock MAINTENANCE_LOCK = new ReentrantLock();
    private static final int NODE_SIZE = 32;
    //private volatile TxMaker tx;
    private volatile DB db;

    public void initDb(String databaseFilePath) throws Exception {
        boolean owned = false;
        try {
            if (!MAINTENANCE_LOCK.tryLock() || db != null) {//tx != null) {
                return;
            }
            owned = true;
//            tx = DBMaker.newFileDB(new File(databaseFilePath))
//                    .randomAccessFileEnableIfNeeded()
//                    .cacheLRUEnable()
//                    .makeTxMaker();
            db = DBMaker.newFileDB(new File(databaseFilePath))
                    .randomAccessFileEnableIfNeeded()
                    .cacheLRUEnable().make();
            createSchema();
        } finally {
            if (owned) {
                MAINTENANCE_LOCK.unlock();
            }
        }
    }

    public void initMemoryDb() throws Exception {
        boolean owned = false;
        try {
            if (!MAINTENANCE_LOCK.tryLock() || db != null) {//tx != null) {
                return;
            }
            owned = true;
//            tx = DBMaker.newMemoryDB()
//                    .makeTxMaker();
            db = DBMaker.newMemoryDB().make();
            createSchema();
        } finally {
            if (owned) {
                MAINTENANCE_LOCK.unlock();
            }
        }
    }


    private void createSchema() {
        //DB db = tx.makeTx();
        if (Boolean.getBoolean(StringConstants.INITIALIZE_DATABASE.getValue())) {
            db.createHashMap(SETTINGS, null, new SettingSerializer());
            db.createHashMap(CONTAINERS, null, new ContainerSerializer());
            db.createHashMap(CUSTOM_OBJECT_FACTORIES, null, new CustomObjectFactorySerializer());
            db.createHashMap(REGISTERED_OBJECT_FACTORIES, null, new RegisteredObjectFactorySerializer());
            db.createHashMap(WEB_APPLICATION_DESCRIPTORS, null, new WebApplicationDescriptorSerializer());
            db.createHashSet(REGISTERED_CONTEXT_PATHS, null);
            Atomic.createLong(db, MOCKED_FIELDS_SEQ, 0);
            Atomic.createInteger(db, STARTUP_COUNTER, 1);
        }
        Atomic.Integer count = Atomic.getInteger(db, STARTUP_COUNTER);
        count.set(count.get() + 1);
        db.commit();
    }

    public Map<String, Setting> getSettings() {
        return doInTransaction(new Executor<Map<String, Setting>>() {
            @Override
            public Map<String, Setting> execute(DatabaseAccessor db) {
                return db.getHashMap(SETTINGS);
            }
        });
    }

    public Map<String, Container> getContainers() {
        return doInTransaction(new Executor<Map<String, Container>>() {
            @Override
            public Map<String, Container> execute(DatabaseAccessor db) {
                return db.getHashMap(CONTAINERS);
            }
        });
    }

    public Map<String, CustomObjectFactory> getCustomObjectFactories() {
        return doInTransaction(new Executor<Map<String, CustomObjectFactory>>() {
            @Override
            public Map<String, CustomObjectFactory> execute(DatabaseAccessor db) {
                return db.getHashMap(CUSTOM_OBJECT_FACTORIES);
            }
        });
    }

    public NavigableSet<MockedFieldContainer> getMockedFields(final String contextPath) {
        return doInTransaction(new Executor<NavigableSet<MockedFieldContainer>>() {
            @Override
            public NavigableSet<MockedFieldContainer> execute(DatabaseAccessor accessor) {
                DB db = accessor.getDb();
                Long id = db.getNameDir().get(contextPath);
                if (id != null) {
                    return db.getTreeSet(contextPath);
                }
                return null;
            }
        });
    }

    public NavigableSet<MockedFieldContainer> registerContextPath(final String contextPath) {
        return doInTransaction(new Executor<NavigableSet<MockedFieldContainer>>() {
            @Override
            public NavigableSet<MockedFieldContainer> execute(DatabaseAccessor accessor) {
                DB db = accessor.getDb();
                NavigableSet<MockedFieldContainer> container =
                        db.createTreeSet(contextPath, NODE_SIZE, new MockedFieldContainerSerializer(),
                                         new MockedFieldContainerSorter());
                getRegisteredContextPaths().add(contextPath);
                return container;
            }
        });
    }

    public Map<String, RegisteredObjectFactory> getRegisteredObjectFactories() {
        return doInTransaction(new Executor<Map<String, RegisteredObjectFactory>>() {
            @Override
            public Map<String, RegisteredObjectFactory> execute(DatabaseAccessor db) {
                return db.getHashMap(REGISTERED_OBJECT_FACTORIES);
            }
        });
    }

    public boolean deleteContextPath(final String contextPath) {
        return doInTransaction(new Executor<Boolean>() {
            @Override
            public Boolean execute(DatabaseAccessor accessor) {
                DB db = accessor.getDb();
                Long id = db.getNameDir().get(contextPath);
                db.getEngine().delete(id, new MockedFieldContainerSerializer());
                getRegisteredContextPaths().remove(contextPath);
                return Boolean.TRUE;
            }
        });
    }

    public long getNextMockedFieldsSequenceValue() {
        return doInTransaction(new Executor<Long>() {
            @Override
            public Long execute(DatabaseAccessor db) {
                return Atomic.getLong(db.getDb(), MOCKED_FIELDS_SEQ).incrementAndGet();
            }
        });
    }

    public Collection<String> getRegisteredContextPaths() {
        return doInTransaction(new Executor<Collection<String>>() {
            @Override
            public Collection<String> execute(DatabaseAccessor db) {
                return db.getHashSet(REGISTERED_CONTEXT_PATHS);
            }
        });
    }

    public Map<String, WebApplicationDescriptor> getWebApplicationDescriptors() {
        return doInTransaction(new Executor<Map<String, WebApplicationDescriptor>>() {
            @Override
            public Map<String, WebApplicationDescriptor> execute(DatabaseAccessor db) {
                return db.getHashMap(WEB_APPLICATION_DESCRIPTORS);
            }
        });
    }

    public int getStartupCount() {
        return doInTransaction(new Executor<Integer>() {
            @Override
            public Integer execute(DatabaseAccessor db) {
                return Atomic.getInteger(db.getDb(), STARTUP_COUNTER).get();
            }
        });
    }

    public void doMaintenance() throws InterruptedException {
        if (MAINTENANCE_LOCK.tryLock(5L, TimeUnit.SECONDS)) {
            //tx.makeTx().compact();
            db.compact();
        } else {
            throw new IllegalStateException("Unable to lock the database");
        }
    }

    public void shutdown() throws InterruptedException {
        if (MAINTENANCE_LOCK.tryLock(5L, TimeUnit.SECONDS)) {
            //tx.close();
            db.close();
        } else {
            throw new IllegalStateException("Unable to lock the database");
        }
    }

    public Transaction getActiveTransaction() {
        String activeTransactionId = TransactionRegistry.getActiveTransactionId();
        if (activeTransactionId != null) {
            return TransactionRegistry.getRegisteredTransaction(activeTransactionId);
        }
        return TransactionRegistry.INACTIVE;
    }

    public Transaction createNewTransaction() {
        //return TransactionRegistry.create(tx);
        return TransactionRegistry.create(db);
    }

    public void removeTransaction(Transaction tx) {
        TransactionRegistry.remove(tx);
    }

    private <T> T doInTransaction(Executor<T> executor) {
        Transaction transaction = getActiveTransaction();
        if (!transaction.isActive()) {
            transaction = createNewTransaction();
        }
        T result = null;
        try {
            result = executor.execute(transaction.getDatabaseAccessor());
            transaction.commit();
        } catch (Exception e) {
            transaction.rollback();
        }
        return result;
    }

    private interface Executor<T> {

        T execute(DatabaseAccessor databaseAccessor);

    }

}
