/*
 * Ejisto, a powerful developer assistant
 *
 * Copyright (C) 2010-2012  Celestino Bellone
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
import com.ejisto.modules.dao.db.EmbeddedDatabaseManager;
import com.ejisto.modules.dao.entities.MockedFieldImpl;
import com.ejisto.modules.repository.MockedFieldsRepository;
import com.ejisto.modules.web.DataSourceHolder;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.lang.annotation.ElementType;
import java.util.List;
import java.util.zip.GZIPOutputStream;

import static org.junit.Assert.*;


/**
 * Created by IntelliJ IDEA.
 * User: celestino
 * Date: 1/2/12
 * Time: 7:10 PM
 */
public class PropertyManagerTest {

    private static final String CTX = "/ejisto";
    private static final String sql = "INSERT INTO MOCKEDFIELDS(CONTEXTPATH,CLASSNAME,FIELDNAME,FIELDTYPE,FIELDVALUE,EXPRESSION,FIELDELEMENTTYPE, ACTIVE) VALUES('/ejisto-test','com.ejisto.hello.beans.HelloWorldBean','description','java.lang.String','description!',null,null,1);";
    private static final String className = SimpleBean.class.getName();

    @BeforeClass
    public static void init() throws Exception {
        System.setProperty("derby.stream.error.field",
                           "com.ejisto.core.classloading.javassist.PropertyManagerTest.DEV_NULL");
        File f = File.createTempFile("ejisto-script", ".tmp");
        f.deleteOnExit();
        GZIPOutputStream stream = new GZIPOutputStream(new FileOutputStream(f));
        stream.write(sql.getBytes());
        stream.flush();
        stream.close();
        System.setProperty(StringConstants.DB_SCRIPT.getValue(), f.getAbsolutePath());
        EmbeddedDatabaseManager dataSource = new EmbeddedDatabaseManager();
        dataSource.initDb();
        DataSourceHolder.setDataSource(dataSource);
    }

    @Test
    public void testMockCollectionField() throws Exception {
        MockedFieldImpl field = new MockedFieldImpl();
        field.setActive(true);
        field.setClassName(className);
        field.setContextPath(CTX);
        field.setFieldName("collectionOfObjects");
        field.setFieldType(List.class.getName());
        field.setExpression("size=10");
        field.setFieldElementType(SimpleBeanSubclass.class.getName());
        MockedFieldsRepository.getInstance().insert(field);

        field = new MockedFieldImpl();
        field.setActive(true);
        field.setClassName(SimpleBeanSubclass.class.getName());
        field.setContextPath(CTX);
        field.setFieldName("testMethod");
        field.setFieldType("java.lang.String");
        field.setFieldValue("test");
        MockedFieldsRepository.getInstance().insert(field);

        List<?> res = PropertyManager.mockField(CTX, "collectionOfObjects", className, List.class, null);
        assertNotNull(res);
        assertEquals(10, res.size());
        for (Object re : res) {
            assertTrue(re instanceof SimpleBeanSubclass);
            assertEquals("test", ((SimpleBeanSubclass) re).getTestMethod());
        }
    }

    @Test
    public void testMockByteField() throws Exception {
        insertField("byteField", "byte", "-1");
        byte res = PropertyManager.mockField(CTX, "byteField", className, (byte) -2);
        assertEquals(-1, res);
    }

    @Test
    public void testMockShortField() throws Exception {
        insertField("shortField", "short", "-42");
        short res = PropertyManager.mockField(CTX, "shortField", className, (short) -5);
        assertEquals(-42, res);
    }

    @Test
    public void testMockIntField() throws Exception {
        insertField("intField", "int", "42");
        int res = PropertyManager.mockField(CTX, "intField", className, -42);
        assertEquals(42, res);
    }

    @Test
    public void testMockLongField() throws Exception {
        insertField("longField", "long", "666");
        long res = PropertyManager.mockField(CTX, "longField", className, -666L);
        assertEquals(666L, res);
    }

    @Test
    public void testMockFloatField() throws Exception {
        insertField("floatField", "float", "42.00");
        float res = PropertyManager.mockField(CTX, "floatField", className, -42.0F);
        assertEquals(42.0F, res, 0.0F);
    }

    @Test
    public void testMockDoubleField() throws Exception {
        insertField("doubleField", "double", "42.00");
        double res = PropertyManager.mockField(CTX, "doubleField", className, -42.0D);
        assertEquals(42.0D, res, 0.0D);
    }

    @Test
    public void testMockCharField() throws Exception {
        insertField("charField", "char", "17");
        char res = PropertyManager.mockField(CTX, "charField", className, (char) 17);
        assertEquals((char) 17, res);
    }

    @Test
    public void testMockBooleanField() throws Exception {
        insertField("booleanField", "boolean", "true");
        boolean res = PropertyManager.mockField(CTX, "booleanField", className, false);
        assertTrue(res);
    }

    @Test
    public void testMockObjectArrayField() throws Exception {
        insertField("stringArrayField", "java.lang.String[]", "one,two,three");
        String[] res = PropertyManager.mockField(CTX, "stringArrayField", className, String[].class, null);
        assertNotNull(res);
        assertTrue(res.length == 3);
        assertEquals("one", res[0]);
        assertEquals("two", res[1]);
        assertEquals("three", res[2]);
    }

    @Test
    public void testMockObjectEnumField() throws Exception {
        insertField("enumField", "java.lang.annotation.ElementType", "METHOD");
        ElementType res = PropertyManager.mockField(CTX, "enumField", className, ElementType.class, null);
        assertNotNull(res);
        assertSame(ElementType.METHOD, res);
    }

    private void insertField(String fieldName, String fieldType, String fieldValue) {
        MockedFieldImpl field = new MockedFieldImpl();
        field.setActive(true);
        field.setClassName(className);
        field.setContextPath(CTX);
        field.setFieldName(fieldName);
        field.setFieldType(fieldType);
        field.setFieldValue(fieldValue);
        MockedFieldsRepository.getInstance().insert(field);
    }

    private static final class SimpleBean {
    }

    private static class SimpleBeanSubclass {
        SimpleBeanSubclass() {

        }

        String getTestMethod() {
            return "";
        }
    }

    public static final OutputStream DEV_NULL = new OutputStream() {
        public void write(int b) { }
    };
}
