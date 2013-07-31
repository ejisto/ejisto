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

package com.ejisto.modules.repository;

import com.ejisto.core.configuration.CoreBundle;
import com.ejisto.modules.dao.db.EmbeddedDatabaseManager;
import com.ejisto.modules.dao.entities.RegisteredObjectFactory;
import com.ejisto.modules.dao.local.LocalObjectFactoryDao;
import org.junit.Test;
import se.jbee.inject.Injector;
import se.jbee.inject.bootstrap.Bootstrap;

import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static se.jbee.inject.Dependency.dependency;

/**
 * Created by IntelliJ IDEA.
 * User: celestino
 * Date: 12/5/11
 * Time: 8:00 AM
 */
public class ObjectFactoryRepositoryTest {

    private static final Injector INJECTOR = Bootstrap.injector(CoreBundle.class);
    private final ObjectFactoryRepository repository;
    private final EmbeddedDatabaseManager db;
    private String context = "/myContext";

    public ObjectFactoryRepositoryTest() {
        db = INJECTOR.resolve(dependency(EmbeddedDatabaseManager.class));
        repository = new ObjectFactoryRepository(null, new LocalObjectFactoryDao(db) {
            @Override
            public List<RegisteredObjectFactory> loadAll() {
                return Collections.emptyList();
            }
        });
    }

    @Test
    public void testStringObjectFactory() {
        assertEquals("com.ejisto.modules.factory.impl.StringFactory",
                     repository.getObjectFactoryClass("java.lang.String", context));
    }

    @Test
    public void testAtomicIntegerFactory() {
        assertEquals("com.ejisto.modules.factory.impl.AtomicIntegerFactory",
                     repository.getObjectFactoryClass("java.util.concurrent.atomic.AtomicInteger", context));
    }

    @Test
    public void testAtomicLongFactory() {
        assertEquals("com.ejisto.modules.factory.impl.AtomicLongFactory",
                     repository.getObjectFactoryClass("java.util.concurrent.atomic.AtomicLong", context));
    }

    @Test
    public void testNumberFactory() {
        assertEquals("com.ejisto.modules.factory.impl.NumberFactory",
                     repository.getObjectFactoryClass("java.lang.Integer", context));
    }

    @Test
    public void testCollectionFactory() {
        assertEquals("com.ejisto.modules.factory.impl.CollectionFactory",
                     repository.getObjectFactoryClass("java.util.Collection", context));
        assertEquals("com.ejisto.modules.factory.impl.CollectionFactory",
                     repository.getObjectFactoryClass("java.util.List", context));
    }

    @Test
    public void testGetMapFactory() {
        assertEquals("com.ejisto.modules.factory.impl.MapFactory",
                     repository.getObjectFactoryClass("java.util.Map", context));
        assertEquals("com.ejisto.modules.factory.impl.MapFactory",
                     repository.getObjectFactoryClass("java.util.TreeMap", context));
    }

    @Test
    public void testGetDefaultObjectFactory() {
        assertEquals("com.ejisto.modules.factory.impl.DefaultObjectFactory",
                     repository.getObjectFactoryClass("java.lang.Object", context));
        assertEquals("com.ejisto.modules.factory.impl.DefaultObjectFactory",
                     repository.getObjectFactoryClass("com.ejisto.modules.cargo.CargoManager", context));
    }

    @Test
    public void testPrimitiveObjectFactory() {
        assertEquals("java.lang.Integer", repository.getActualType("int"));
        assertEquals("java.lang.Long", repository.getActualType("long"));
        assertEquals("java.lang.Long", repository.getActualType("java.lang.Long"));
        assertEquals("com.ejisto.modules.factory.impl.NumberFactory", repository.getObjectFactoryClass("int", context));
    }

    @Test
    public void testArrayObjectFactory() {
        assertEquals("java.lang.Integer", repository.getActualType("[Lint;"));
        assertEquals("java.lang.Long", repository.getActualType("long[]"));
        assertEquals("java.lang.Long", repository.getActualType("java.lang.Long[]"));
        assertEquals("java.lang.Long", repository.getActualType("[Ljava.lang.Long;"));
    }

}
