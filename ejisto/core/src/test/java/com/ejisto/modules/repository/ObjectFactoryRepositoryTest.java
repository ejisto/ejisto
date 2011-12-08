/*
 * Ejisto, a powerful developer assistant
 *
 * Copyright (C) 2010-2011  Celestino Bellone
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

import com.ejisto.modules.dao.ObjectFactoryDao;
import com.ejisto.modules.dao.entities.ObjectFactory;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Created by IntelliJ IDEA.
 * User: celestino
 * Date: 12/5/11
 * Time: 8:00 AM
 */
public class ObjectFactoryRepositoryTest {

    private ObjectFactoryRepository repository;
    private String context = "/myContext";

    @Before
    public void init() {
        repository = ObjectFactoryRepository.getInstance();
        repository.setDaoInstance(new ObjectFactoryDao() {
            @Override
            public List<ObjectFactory> loadAll() {
                return Collections.emptyList();
            }
        });
    }

    @Test
    public void testStringObjectFactory() {
        assertEquals("com.ejisto.modules.factory.impl.StringFactory",
                     repository.getObjectFactory("java.lang.String", context));
    }

    @Test
    public void testAtomicIntegerFactory() {
        assertEquals("com.ejisto.modules.factory.impl.AtomicIntegerFactory",
                     repository.getObjectFactory("java.util.concurrent.atomic.AtomicInteger", context));
    }

    @Test
    public void testAtomicLongFactory() {
        assertEquals("com.ejisto.modules.factory.impl.AtomicLongFactory",
                     repository.getObjectFactory("java.util.concurrent.atomic.AtomicLong", context));
    }

    @Test
    public void testNumberFactory() {
        assertEquals("com.ejisto.modules.factory.impl.NumberFactory",
                     repository.getObjectFactory("java.lang.Integer", context));
    }

    @Test
    public void testCollectionFactory() {
        assertEquals("com.ejisto.modules.factory.impl.CollectionFactory",
                     repository.getObjectFactory("java.util.Collection", context));
    }

    @Test
    public void testGetMapFactory() {
        assertEquals("com.ejisto.modules.factory.impl.MapFactory",
                     repository.getObjectFactory("java.util.Map", context));
    }

    @Test
    public void testGetDefaultObjectFactory() {
        assertEquals("com.ejisto.modules.factory.impl.DefaultObjectFactory",
                     repository.getObjectFactory("java.lang.Object", context));
    }

    @Test
    public void testPrimitiveObjectFactory() {
        assertEquals("java.lang.Integer", repository.transformPrimitiveType("int"));
        assertEquals("java.lang.Long", repository.transformPrimitiveType("long"));
        assertEquals("java.lang.Long", repository.transformPrimitiveType("java.lang.Long"));
        assertEquals("com.ejisto.modules.factory.impl.NumberFactory", repository.getObjectFactory("int", context));
    }

}
