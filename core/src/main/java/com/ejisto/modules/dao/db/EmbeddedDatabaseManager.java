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

import java.io.File;
import java.util.Collection;

import static com.ejisto.constants.StringConstants.DB_SCRIPT;

@Log4j
public class EmbeddedDatabaseManager {

    private static final String SETTINGS = "settings";
    private static final String CONTAINERS = "containers";
    private static final String CUSTOM_OBJECT_FACTORIES = "customObjectFactories";
    private static final String JNDI_DATA_SOURCES = "jndiDataSources";
    private static final String JNDI_DATA_SOURCES_SEQ = "jndiDataSourcesSeq";
    private static final String REGISTERED_OBJECT_FACTORIES = "registeredObjectFactories";
    private static final String WEB_APPLICATION_DESCRIPTORS = "webApplicationDescriptors";
    private static final String WEB_APPLICATION_DESCRIPTORS_SEQ = "webApplicationDescriptorsSeq";
    private static final String REGISTERED_CONTEXT_PATHS = "registeredContextPaths";
    private static final String MOCKED_FIELDS_SEQ = "mockedFieldsSeq";

    private final DB db;

    public EmbeddedDatabaseManager() {
        this.db = DBMaker.newFileDB(new File(System.getProperty(DB_SCRIPT.getValue())))
                .closeOnJvmShutdown()
                .randomAccessFileEnableIfNeeded()
                .compressionEnable()
                .make();
    }

    @SuppressWarnings("unchecked")
    public void initDb() throws Exception {
        if (Boolean.getBoolean(StringConstants.INITIALIZE_DATABASE.getValue())) {
            db.createHashSet(SETTINGS, new JSONSerializer<>(Setting.class));
            db.createHashSet(CONTAINERS, new JSONSerializer<>(Container.class));
            db.createHashSet(CUSTOM_OBJECT_FACTORIES, new JSONSerializer<>(CustomObjectFactory.class));
            db.createHashSet(JNDI_DATA_SOURCES, new JSONSerializer<>(JndiDataSource.class));
            Atomic.createLong(db, JNDI_DATA_SOURCES_SEQ, 0);
            db.createHashSet(REGISTERED_OBJECT_FACTORIES, new JSONSerializer<>(RegisteredObjectFactory.class));
            db.createHashSet(WEB_APPLICATION_DESCRIPTORS, new JSONSerializer<>(WebApplicationDescriptor.class));
            db.createHashSet(REGISTERED_CONTEXT_PATHS, db.getDefaultSerializer());
            Atomic.createLong(db, MOCKED_FIELDS_SEQ, 0);
            Atomic.createLong(db, WEB_APPLICATION_DESCRIPTORS_SEQ, 0);
        }
    }

    public Collection<Setting> getSettings() {
        return db.getHashSet(SETTINGS);
    }

    public Collection<Container> getContainers() {
        return db.getHashSet(CONTAINERS);
    }

    public Collection<CustomObjectFactory> getCustomObjectFactories() {
         return db.getHashSet(CUSTOM_OBJECT_FACTORIES);
    }

    public Collection<JndiDataSource> getJndiDataSources() {
        return db.getHashSet(JNDI_DATA_SOURCES);
    }

    public long getNextJndiDataSourceSequenceValue() {
        return Atomic.getLong(db, JNDI_DATA_SOURCES_SEQ).incrementAndGet();
    }

    public Collection<MockedField> getMockedFields(String contextPath) {
        Collection<MockedField> out = db.getHashSet(contextPath);
        db.getHashSet(REGISTERED_CONTEXT_PATHS).add(contextPath);
        return out;
    }

    public Collection<RegisteredObjectFactory> getRegisteredObjectFactories() {
        return db.getHashSet(REGISTERED_OBJECT_FACTORIES);
    }

    public void deleteContextPath(String contextPath) {
        Long id = db.getNameDir().get(contextPath);
        db.getEngine().delete(id);
        getRegisteredContextPaths().remove(contextPath);
    }

    public long getNextMockedFieldsSequenceValue() {
            return Atomic.getLong(db, MOCKED_FIELDS_SEQ).incrementAndGet();
    }
    public long getNextWebApplicationDescriptorSequenceValue() {
            return Atomic.getLong(db, WEB_APPLICATION_DESCRIPTORS_SEQ).incrementAndGet();
    }

    public Collection<String> getRegisteredContextPaths() {
        return db.getHashSet(REGISTERED_CONTEXT_PATHS);
    }

    public Collection<WebApplicationDescriptor> getWebApplicationDescriptors() {
        return db.getHashSet(WEB_APPLICATION_DESCRIPTORS);
    }

    public void commit() {
        db.commit();
    }

    public void rollback() {
        db.rollback();
    }


}
