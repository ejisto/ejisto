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

import ch.lambdaj.function.convert.Converter;
import com.ejisto.constants.StringConstants;
import com.ejisto.modules.dao.db.util.MockedFieldContainer;
import com.ejisto.modules.dao.db.util.MockedFieldContainerSorter;
import com.ejisto.modules.dao.db.util.serializer.*;
import com.ejisto.modules.dao.entities.*;
import lombok.extern.log4j.Log4j;
import org.apache.commons.lang3.StringUtils;
import org.mapdb.Atomic;
import org.mapdb.DB;
import org.mapdb.DBMaker;

import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.NavigableSet;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import static ch.lambdaj.Lambda.convert;
import static ch.lambdaj.Lambda.select;
import static org.hamcrest.Matchers.startsWith;

@Log4j
public class EmbeddedDatabaseManager {

    private static final String CONTEXT_PATH_PREFIX = "__CTX__";
    private static final String SETTINGS = "settings";
    private static final String CONTAINERS = "containers";
    private static final String CUSTOM_OBJECT_FACTORIES = "customObjectFactories";
    private static final String REGISTERED_OBJECT_FACTORIES = "registeredObjectFactories";
    private static final String WEB_APPLICATION_DESCRIPTORS = "webApplicationDescriptors";
    private static final String STARTUP_COUNTER = "startupCounter";
    private static final ReentrantLock MAINTENANCE_LOCK = new ReentrantLock();
    private static final int NODE_SIZE = 32;
    private volatile DB db;
    private final CopyOnWriteArrayList<String> contextPaths = new CopyOnWriteArrayList<>();

    public void initDb(String databaseFilePath) throws Exception {
        boolean owned = false;
        try {
            if (!MAINTENANCE_LOCK.tryLock() || db != null) {
                return;
            }
            owned = true;
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
            if (!MAINTENANCE_LOCK.tryLock() || db != null) {
                return;
            }
            owned = true;
            db = DBMaker.newMemoryDB().make();
            createSchema();
        } finally {
            if (owned) {
                MAINTENANCE_LOCK.unlock();
            }
        }
    }


    private void createSchema() {
        if (Boolean.getBoolean(StringConstants.INITIALIZE_DATABASE.getValue())) {
            db.createHashMap(SETTINGS, false, null, new SettingSerializer());
            db.createHashMap(CONTAINERS, false, null, new ContainerSerializer());
            db.createHashMap(CUSTOM_OBJECT_FACTORIES, false, null, new CustomObjectFactorySerializer());
            db.createHashMap(REGISTERED_OBJECT_FACTORIES, false, null, new RegisteredObjectFactorySerializer());
            db.createHashMap(WEB_APPLICATION_DESCRIPTORS, false, null, new WebApplicationDescriptorSerializer());
            Atomic.createInteger(db, STARTUP_COUNTER, 1);
        }
        contextPaths.addAll(
                convert(
                        select(db.getNameDir().keySet(), startsWith(CONTEXT_PATH_PREFIX)),
                        new Converter<String, String>() {
                            @Override
                            public String convert(String from) {
                                return decodeContextPath(from);
                            }
                        }));
        Atomic.Integer count = Atomic.getInteger(db, STARTUP_COUNTER);
        count.incrementAndGet();
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
                String key = encodeContextPath(contextPath);
                Long id = db.getNameDir().get(key);
                if (id != null) {
                    return db.getTreeSet(key);
                }
                return null;
            }
        });
    }

    public void registerContextPath(final String contextPath) {
        doInTransaction(new Executor<Void>() {
            @Override
            public Void execute(DatabaseAccessor accessor) {
                DB db = accessor.getDb();
                db.createTreeSet(encodeContextPath(contextPath), NODE_SIZE, false, new MockedFieldContainerSerializer(),
                                 new MockedFieldContainerSorter());
                contextPaths.add(contextPath);
                return null;
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

    public boolean deleteAllMockedFields(final String contextPath) {
        return doInTransaction(new Executor<Boolean>() {
            @Override
            public Boolean execute(DatabaseAccessor accessor) {
                DB db = accessor.getDb();
                String key = encodeContextPath(contextPath);
                db.getTreeSet(key).clear();
                return Boolean.TRUE;
            }
        });
    }

    public Collection<String> getRegisteredContextPaths() {
        return Collections.unmodifiableList(contextPaths);
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
            db.compact();
        } else {
            throw new IllegalStateException("Unable to lock the database");
        }
    }

    public void shutdown() throws InterruptedException {
        if (MAINTENANCE_LOCK.tryLock(5L, TimeUnit.SECONDS)) {
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
        return TransactionRegistry.create(db);
    }

    public void removeTransaction(Transaction tx) {
        TransactionRegistry.remove(tx);
    }

    private static String encodeContextPath(String contextPath) {
        return CONTEXT_PATH_PREFIX + contextPath;
    }

    private static String decodeContextPath(String encoded) {
        return StringUtils.substring(encoded, CONTEXT_PATH_PREFIX.length());
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
