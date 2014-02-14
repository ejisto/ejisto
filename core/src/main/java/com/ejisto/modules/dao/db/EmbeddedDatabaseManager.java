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
import com.ejisto.modules.dao.db.util.serializer.*;
import com.ejisto.modules.dao.entities.*;
import com.ejisto.modules.recorder.CollectedData;
import lombok.extern.log4j.Log4j;
import org.apache.commons.lang3.StringUtils;
import org.mapdb.Atomic;
import org.mapdb.BTreeMap;
import org.mapdb.DB;
import org.mapdb.DBMaker;

import java.io.File;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

@Log4j
public class EmbeddedDatabaseManager {

    private static final String CONTEXT_PATH_PREFIX = "__CTX__";
    private static final String RECORDED_SESSIONS = "recordedSessions";
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

    public void initDb(String databaseFilePath) {
        boolean owned = false;
        try {
            if (!MAINTENANCE_LOCK.tryLock() || db != null) {
                return;
            }
            owned = true;
            db = DBMaker.newFileDB(new File(databaseFilePath))
                    .cacheLRUEnable().make();
            createSchema();
        } finally {
            if (owned) {
                MAINTENANCE_LOCK.unlock();
            }
        }
    }

    public void initMemoryDb() {
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
            //String name, boolean keepCounter, Serializer<K> keySerializer, Serializer<V> valueSerializer
            db.createHashMap(SETTINGS).valueSerializer(new SettingSerializer()).make();
            db.createHashMap(CONTAINERS).counterEnable().valueSerializer(new ContainerSerializer()).make();
            db.createHashMap(CUSTOM_OBJECT_FACTORIES).counterEnable().valueSerializer(new CustomObjectFactorySerializer()).make();
            db.createHashMap(REGISTERED_OBJECT_FACTORIES).counterEnable().valueSerializer(new RegisteredObjectFactorySerializer()).make();
            db.createHashMap(WEB_APPLICATION_DESCRIPTORS).counterEnable().valueSerializer(new WebApplicationDescriptorSerializer()).make();
            db.createHashMap(RECORDED_SESSIONS).counterEnable().valueSerializer(new CollectedDataSerializer()).make();
            db.createAtomicInteger(STARTUP_COUNTER, 1);
        }
        contextPaths.addAll(db.getAll().keySet().stream()
                                    .filter(k -> k.startsWith(CONTEXT_PATH_PREFIX))
                                    .map(EmbeddedDatabaseManager::decodeContextPath)
                                    .collect(toList()));
        Atomic.Integer count = db.getAtomicInteger(STARTUP_COUNTER);
        count.incrementAndGet();
        db.commit();
    }

    public Optional<Map<String, Setting>> getSettings() {
        return doInTransaction(accessor -> accessor.getHashMap(SETTINGS));
    }

    public Optional<Map<String, Container>> getContainers() {
        return doInTransaction(db -> db.getHashMap(CONTAINERS));
    }

    public Optional<Map<String, CustomObjectFactory>> getCustomObjectFactories() {
        return doInTransaction(db -> db.getHashMap(CUSTOM_OBJECT_FACTORIES));
    }

    public Optional<NavigableSet<MockedFieldContainer>> getMockedFields(final String contextPath) {
        return doInTransaction(accessor -> {
            DB db = accessor.getDb();
            String key = encodeContextPath(contextPath);
            if(db.exists(key)) {
                return db.get(key);
            }
            return null;
        });
    }

    public void registerContextPath(final String contextPath) {
        doInTransaction(accessor -> {
            DB db = accessor.getDb();
            db.createTreeSet(encodeContextPath(contextPath))
                    .nodeSize(NODE_SIZE)
                    .counterEnable()
                    .serializer(new MockedFieldContainerSerializer())
                    .comparator(BTreeMap.COMPARABLE_COMPARATOR).make();
            contextPaths.add(contextPath);
            return null;
        });
    }

    public void createNewRecordingSession(final String name, final CollectedData collectedData) {
        doInTransaction(databaseAccessor -> {
            DB db = databaseAccessor.getDb();
            db.getHashMap(RECORDED_SESSIONS).put(name, collectedData);
            return null;
        });
    }

    public Optional<Map<String, RegisteredObjectFactory>> getRegisteredObjectFactories() {
        return doInTransaction(accessor -> accessor.getHashMap(REGISTERED_OBJECT_FACTORIES));
    }

    public Boolean deleteAllMockedFields(final String contextPath) {
        return doInTransaction(accessor -> {
            DB db = accessor.getDb();
            String key = encodeContextPath(contextPath);
            db.delete(key);
            contextPaths.remove(key);
            internalGetWebApplicationDescriptors(accessor).remove(contextPath);
            return Boolean.TRUE;
        }).orElse(Boolean.FALSE);
    }

    public Optional<CollectedData> getRecordedSession(final String name) {
        return doInTransaction(databaseAccessor -> {
            Map<String, CollectedData> map = databaseAccessor.getHashMap(RECORDED_SESSIONS);
            return map.get(name);
        });
    }

    public Optional<Collection<CollectedData>> getActiveRecordedSessions() {
        return doInTransaction(databaseAccessor -> {
            Map<String, CollectedData> map = databaseAccessor.getHashMap(RECORDED_SESSIONS);
            return map.values().stream().filter(CollectedData::isActive).collect(Collectors.toList());
        });
    }

    public Collection<String> getRegisteredContextPaths() {
        return Collections.unmodifiableList(contextPaths);
    }

    public Optional<Map<String, CollectedData>> getRecordedSessions() {
        return doInTransaction(databaseAccessor -> Collections.unmodifiableMap(
                databaseAccessor.getHashMap(RECORDED_SESSIONS)));
    }


    public Optional<Map<String, WebApplicationDescriptor>> getWebApplicationDescriptors() {
        return doInTransaction(this::internalGetWebApplicationDescriptors);
    }

    private Map<String, WebApplicationDescriptor> internalGetWebApplicationDescriptors(DatabaseAccessor db) {
        return db.getHashMap(WEB_APPLICATION_DESCRIPTORS);
    }

    public Optional<Integer> getStartupCount() {
        return doInTransaction(accessor -> accessor.getDb().getAtomicInteger(STARTUP_COUNTER).get());
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

    private <T> Optional<T> doInTransaction(Executor<T> executor) {
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
        return Optional.ofNullable(result);
    }

    @FunctionalInterface
    private interface Executor<T> {
        T execute(DatabaseAccessor databaseAccessor);
    }

}
