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
import com.ejisto.modules.dao.db.EmbeddedDatabaseManager;
import com.ejisto.modules.dao.entities.RegisteredObjectFactory;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;

import static org.junit.Assert.assertNotNull;

/**
 * Created by IntelliJ IDEA.
 * User: celestino
 * Date: 3/23/13
 * Time: 10:08 PM
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ActiveProfiles(value = {"test", "server"})
@ContextConfiguration(value = {"classpath:/core-context.xml"})
public class ObjectFactoryDaoTest {

    @Resource private EmbeddedDatabaseManager db;
    @Resource private ObjectFactoryDao dao;

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
