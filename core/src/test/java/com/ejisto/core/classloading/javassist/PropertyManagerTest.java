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

package com.ejisto.core.classloading.javassist;

import com.ejisto.constants.StringConstants;
import com.ejisto.core.configuration.CoreBundle;
import com.ejisto.modules.dao.db.EmbeddedDatabaseManager;
import com.ejisto.modules.dao.entities.MockedField;
import com.ejisto.modules.dao.entities.MockedFieldImpl;
import com.ejisto.modules.factory.ObjectFactory;
import com.ejisto.modules.repository.MockedFieldsRepository;
import com.ejisto.modules.repository.ObjectFactoryRepository;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import se.jbee.inject.Injector;
import se.jbee.inject.bootstrap.Bootstrap;

import java.lang.annotation.ElementType;
import java.util.List;

import static org.junit.Assert.*;
import static se.jbee.inject.Dependency.dependency;


/**
 * Created by IntelliJ IDEA.
 * User: celestino
 * Date: 1/2/12
 * Time: 7:10 PM
 */
public class PropertyManagerTest {

    private static final long TIMEOUT = 5000L;
    private static final String CTX = "/ejisto";
    private static final String CLASS_NAME = SimpleBean.class.getName();
    private static final Injector INJECTOR = Bootstrap.injector(CoreBundle.class);
    private static final ObjectFactoryRepository OBJECT_FACTORY_REPOSITORY;
    private static final MockedFieldsRepository MOCKED_FIELDS_REPOSITORY;
    private static final EmbeddedDatabaseManager DB;
    private static final PropertyManager PROPERTY_MANAGER;

    static {
        DB = INJECTOR.resolve(dependency(EmbeddedDatabaseManager.class));
        OBJECT_FACTORY_REPOSITORY = INJECTOR.resolve(dependency(ObjectFactoryRepository.class));
        MOCKED_FIELDS_REPOSITORY = INJECTOR.resolve(dependency(MockedFieldsRepository.class));
        PROPERTY_MANAGER = INJECTOR.resolve(dependency(PropertyManager.class));
    }

    @BeforeClass
    public static void initClass() {
        System.setProperty(StringConstants.INITIALIZE_DATABASE.getValue(), "true");
    }

    @Before
    public void init() throws Exception {
        DB.initMemoryDb();
        OBJECT_FACTORY_REPOSITORY.registerObjectFactory(SimpleBeanObjectFactory.class.getName(),
                                                      SimpleBean.class.getName(), false);
    }

    @Test(timeout = TIMEOUT)
    public void testMockCollectionField() throws Exception {
        MockedFieldImpl field = new MockedFieldImpl();
        field.setActive(true);
        field.setClassName(CLASS_NAME);
        field.setContextPath(CTX);
        field.setFieldName("collectionOfObjects");
        field.setFieldType(List.class.getName());
        field.setExpression("size=10");
        field.setFieldElementType(AnotherSimpleBean.class.getName());
        MOCKED_FIELDS_REPOSITORY.insert(field);

        field = new MockedFieldImpl();
        field.setActive(true);
        field.setClassName(AnotherSimpleBean.class.getName());
        field.setContextPath(CTX);
        field.setFieldName("testMethod");
        field.setFieldType("java.lang.String");
        field.setFieldValue("test");
        MOCKED_FIELDS_REPOSITORY.insert(field);

        List<?> res = PROPERTY_MANAGER.mockField(CTX, "collectionOfObjects", CLASS_NAME, List.class, null);
        assertNotNull(res);
        assertEquals(10, res.size());
        for (Object re : res) {
            assertTrue(re instanceof AnotherSimpleBean);
            assertEquals("test", ((AnotherSimpleBean) re).getTestMethod());
        }
    }

    @Test(timeout = TIMEOUT)
    public void testMockByteField() throws Exception {
        insertField("byteField", "byte", "-1");
        byte res = PROPERTY_MANAGER.mockField(CTX, "byteField", CLASS_NAME, (byte) -2);
        assertEquals(-1, res);
    }

    @Test(timeout = TIMEOUT)
    public void testMockShortField() throws Exception {
        insertField("shortField", "short", "-42");
        short res = PROPERTY_MANAGER.mockField(CTX, "shortField", CLASS_NAME, (short) -5);
        assertEquals(-42, res);
    }

    @Test(timeout = TIMEOUT)
    public void testMockIntField() throws Exception {
        insertField("intField", "int", "42");
        int res = PROPERTY_MANAGER.mockField(CTX, "intField", CLASS_NAME, -42);
        assertEquals(42, res);
    }

    @Test(timeout = TIMEOUT)
    public void testMockLongField() throws Exception {
        insertField("longField", "long", "666");
        long res = PROPERTY_MANAGER.mockField(CTX, "longField", CLASS_NAME, -666L);
        assertEquals(666L, res);
    }

    @Test(timeout = TIMEOUT)
    public void testMockFloatField() throws Exception {
        insertField("floatField", "float", "42.00");
        float res = PROPERTY_MANAGER.mockField(CTX, "floatField", CLASS_NAME, -42.0F);
        assertEquals(42.0F, res, 0.0F);
    }

    @Test(timeout = TIMEOUT)
    public void testMockDoubleField() throws Exception {
        insertField("doubleField", "double", "42.00");
        double res = PROPERTY_MANAGER.mockField(CTX, "doubleField", CLASS_NAME, -42.0D);
        assertEquals(42.0D, res, 0.0D);
    }

    @Test(timeout = TIMEOUT)
    public void testMockCharField() throws Exception {
        insertField("charField", "char", "17");
        char res = PROPERTY_MANAGER.mockField(CTX, "charField", CLASS_NAME, (char) 17);
        assertEquals((char) 17, res);
    }

    @Test(timeout = TIMEOUT)
    public void testMockBooleanField() throws Exception {
        insertField("booleanField", "boolean", "true");
        boolean res = PROPERTY_MANAGER.mockField(CTX, "booleanField", CLASS_NAME, false);
        assertTrue(res);
    }

    @Test(timeout = TIMEOUT)
    public void testMockObjectArrayField() throws Exception {
        insertField("stringArrayField", "java.lang.String[]", "one,two,three");
        String[] res = PROPERTY_MANAGER.mockField(CTX, "stringArrayField", CLASS_NAME, String[].class, null);
        assertNotNull(res);
        assertTrue(res.length == 3);
        assertEquals("one", res[0]);
        assertEquals("two", res[1]);
        assertEquals("three", res[2]);
    }

    @Test(timeout = TIMEOUT)
    public void testMockObjectEnumField() throws Exception {
        insertField("enumField", "java.lang.annotation.ElementType", "METHOD");
        ElementType res = PROPERTY_MANAGER.mockField(CTX, "enumField", CLASS_NAME, ElementType.class, null);
        assertNotNull(res);
        assertSame(ElementType.METHOD, res);
    }

    private void insertField(String fieldName, String fieldType, String fieldValue) {
        MockedFieldImpl field = new MockedFieldImpl();
        field.setActive(true);
        field.setClassName(CLASS_NAME);
        field.setContextPath(CTX);
        field.setFieldName(fieldName);
        field.setFieldType(fieldType);
        field.setFieldValue(fieldValue);
        MOCKED_FIELDS_REPOSITORY.insert(field);
    }

    private static class SimpleBean {
    }

    private static class AnotherSimpleBean {
        AnotherSimpleBean() {

        }

        String getTestMethod() {
            return "";
        }
    }

    public static class SimpleBeanObjectFactory implements ObjectFactory<SimpleBean> {

        @Override
        public String getTargetClassName() {
            return SimpleBean.class.getName();
        }

        @Override
        public SimpleBean create(MockedField m, SimpleBean actualValue) {
            return new SimpleBean();
        }

        @Override
        public boolean supportsRandomValuesCreation() {
            return true;
        }

        @Override
        public SimpleBean createRandomValue() {
            return new SimpleBean();
        }
    }
}
