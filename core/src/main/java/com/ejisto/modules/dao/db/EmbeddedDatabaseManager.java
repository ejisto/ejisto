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
import com.ejisto.modules.dao.entities.*;
import lombok.extern.log4j.Log4j;
import org.mapdb.Atomic;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.Serializer;

import java.io.File;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

@Log4j
public class EmbeddedDatabaseManager {

    private static final String SETTINGS = "settings";
    private static final String CONTAINERS = "containers";
    private static final String CUSTOM_OBJECT_FACTORIES = "customObjectFactories";
    private static final String REGISTERED_OBJECT_FACTORIES = "registeredObjectFactories";
    private static final String WEB_APPLICATION_DESCRIPTORS = "webApplicationDescriptors";
    private static final String WEB_APPLICATION_DESCRIPTORS_SEQ = "webApplicationDescriptorsSeq";
    private static final String REGISTERED_CONTEXT_PATHS = "registeredContextPaths";
    private static final String MOCKED_FIELDS_SEQ = "mockedFieldsSeq";
    private static final String STARTUP_COUNTER = "startupCounter";
    private static final ReentrantLock MAINTENANCE_LOCK = new ReentrantLock();

    private volatile DB db;

    public void initDb(String databaseFilePath) throws Exception {
        try {
            if (!MAINTENANCE_LOCK.tryLock() || db != null) {
                return;
            }
            db = DBMaker.newFileDB(new File(databaseFilePath))
                    .closeOnJvmShutdown()
                    .randomAccessFileEnableIfNeeded()
                    .compressionEnable()
                    .make();
            if (Boolean.getBoolean(StringConstants.INITIALIZE_DATABASE.getValue())) {
                db.createHashMap(SETTINGS, Serializer.STRING_SERIALIZER, new JSONSerializer<>(Setting.class));
                db.createHashMap(CONTAINERS, Serializer.STRING_SERIALIZER, new JSONSerializer<>(Container.class));
                db.createHashMap(CUSTOM_OBJECT_FACTORIES, Serializer.STRING_SERIALIZER,
                                 new JSONSerializer<>(CustomObjectFactory.class));
                db.createHashMap(REGISTERED_OBJECT_FACTORIES, Serializer.STRING_SERIALIZER,
                                 new JSONSerializer<>(RegisteredObjectFactory.class));
                db.createHashMap(WEB_APPLICATION_DESCRIPTORS, Serializer.STRING_SERIALIZER,
                                 new JSONSerializer<>(WebApplicationDescriptor.class));
                db.createHashMap(REGISTERED_CONTEXT_PATHS, Serializer.STRING_SERIALIZER, null);
                Atomic.createLong(db, MOCKED_FIELDS_SEQ, 0);
                Atomic.createLong(db, WEB_APPLICATION_DESCRIPTORS_SEQ, 0);
                Atomic.createInteger(db, STARTUP_COUNTER, 1);
            }
            Atomic.getInteger(db, STARTUP_COUNTER).incrementAndGet();
        } finally {
            MAINTENANCE_LOCK.unlock();
        }
    }

    public Map<String, Setting> getSettings() {
        return db.getHashMap(SETTINGS);
    }

    public Map<String, Container> getContainers() {
        return db.getHashMap(CONTAINERS);
    }

    public Map<String, CustomObjectFactory> getCustomObjectFactories() {
        return db.getHashMap(CUSTOM_OBJECT_FACTORIES);
    }

    public Map<Long, MockedField> getMockedFields(String contextPath) {
        Long id = db.getNameDir().get(contextPath);
        if (id != null) {
            return db.getHashMap(contextPath);
        }
        Map<Long, MockedField> out = db.createHashMap(contextPath, Serializer.LONG_SERIALIZER,
                                                      new JSONSerializer<>(MockedField.class));
        getRegisteredContextPaths().add(contextPath);
        return out;
    }

    public Map<String, RegisteredObjectFactory> getRegisteredObjectFactories() {
        return db.getHashMap(REGISTERED_OBJECT_FACTORIES);
    }

    public void deleteContextPath(String contextPath) {
        Long id = db.getNameDir().get(contextPath);
        db.getEngine().delete(id, new JSONSerializer<>(MockedField.class));
        getRegisteredContextPaths().remove(contextPath);
    }

    public long getNextMockedFieldsSequenceValue() {
        return Atomic.getLong(db, MOCKED_FIELDS_SEQ).incrementAndGet();
    }

    public Collection<String> getRegisteredContextPaths() {
        return db.getHashSet(REGISTERED_CONTEXT_PATHS);
    }

    public Collection<WebApplicationDescriptor> getWebApplicationDescriptors() {
        return db.getHashSet(WEB_APPLICATION_DESCRIPTORS);
    }

    public int getStartupCount() {
        return Atomic.getInteger(db, STARTUP_COUNTER).get();
    }
    public void commit() {
        db.commit();
    }

    public void rollback() {
        db.rollback();
    }

    public void doMaintenance() throws InterruptedException {
        if(MAINTENANCE_LOCK.tryLock(5L, TimeUnit.SECONDS)) {
            db.compact();
        } else {
            throw new IllegalStateException("Unable to lock the database");
        }
    }

    public void shutdown() throws InterruptedException {
        if(MAINTENANCE_LOCK.tryLock(5L, TimeUnit.SECONDS)) {
            db.close();
        } else {
            throw new IllegalStateException("Unable to lock the database");
        }
    }




}
