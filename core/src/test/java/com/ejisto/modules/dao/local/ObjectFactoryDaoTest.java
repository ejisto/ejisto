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

package com.ejisto.modules.dao.local;

import com.ejisto.constants.StringConstants;
import com.ejisto.core.configuration.CoreBundle;
import com.ejisto.modules.dao.db.EmbeddedDatabaseManager;
import com.ejisto.modules.dao.entities.RegisteredObjectFactory;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import se.jbee.inject.Injector;
import se.jbee.inject.bootstrap.Bootstrap;

import static org.junit.Assert.assertNotNull;
import static se.jbee.inject.Dependency.dependency;

/**
 * Created by IntelliJ IDEA.
 * User: celestino
 * Date: 3/23/13
 * Time: 10:08 PM
 */
public class ObjectFactoryDaoTest {

    private static final Injector INJECTOR = Bootstrap.injector(CoreBundle.class);
    private final EmbeddedDatabaseManager db;
    private final ObjectFactoryDao dao;

    public ObjectFactoryDaoTest() {
        this.db = INJECTOR.resolve(dependency(EmbeddedDatabaseManager.class));;
        this.dao = INJECTOR.resolve(dependency(ObjectFactoryDao.class));
    }

    @BeforeClass
    public static void initClass() {
        System.setProperty(StringConstants.INITIALIZE_DATABASE.getValue(), "true");
    }

    @Before
    public void setUp() throws Exception {
        db.initMemoryDb();
    }

    @Test
    public void testLoadAll() {
        assertNotNull(dao.loadAll());
    }

    @Test
    public void testInsert() {
        dao.insert(new RegisteredObjectFactory("myStringFactory", "java.lang.String"));
    }
}
