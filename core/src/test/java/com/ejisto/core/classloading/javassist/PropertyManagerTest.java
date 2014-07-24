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

import com.ejisto.modules.dao.entities.MockedField;
import com.ejisto.modules.dao.entities.MockedFieldImpl;
import com.ejisto.modules.factory.ObjectFactory;
import com.ejisto.modules.factory.impl.*;
import com.ejisto.modules.repository.MockedFieldsRepository;
import com.ejisto.modules.repository.ObjectFactoryRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.lang.annotation.ElementType;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;


/**
 * Created by IntelliJ IDEA.
 * User: celestino
 * Date: 1/2/12
 * Time: 7:10 PM
 */
@RunWith(MockitoJUnitRunner.class)
public class PropertyManagerTest {

    private static final String CTX = "/ejisto";
    private static final String CLASS_NAME = SimpleBean.class.getName();
    private PropertyManager propertyManager;
    @Mock
    private MockedFieldsRepository mockedFieldsRepository;
    @Mock
    private ObjectFactoryRepository objectFactoryRepository;

    @Before
    public void setUp() {
        propertyManager = new PropertyManager(mockedFieldsRepository, objectFactoryRepository);
    }

    @Test
    public void testMockCollectionField() throws Exception {
        MockedFieldImpl field = new MockedFieldImpl();
        field.setActive(true);
        field.setClassName(CLASS_NAME);
        field.setContextPath(CTX);
        field.setFieldName("collectionOfObjects");
        field.setFieldType(List.class.getName());
        //field.setExpression("");
        field.setFieldElementType(AnotherSimpleBean.class.getName());
        when(mockedFieldsRepository.load(CTX, CLASS_NAME, "collectionOfObjects")).thenReturn(field);

        MockedField field2 = new MockedFieldImpl();
        field2.setActive(true);
        field2.setClassName(AnotherSimpleBean.class.getName());
        field2.setContextPath(CTX);
        field2.setFieldName("testMethod");
        field2.setFieldType("java.lang.String");
        field2.setFieldValue("test");
        when(mockedFieldsRepository.load(CTX, AnotherSimpleBean.class.getName(), "testMethod")).thenReturn(field2);
        when(mockedFieldsRepository.loadActiveFields(CTX, AnotherSimpleBean.class.getName())).thenReturn(
                Arrays.asList(field2));
        when(mockedFieldsRepository.load(CTX, AnotherSimpleBean.class.getName())).thenReturn(Arrays.asList(field2));
        CollectionFactory<AnotherSimpleBean> factory = new FakeCollectionFactory<>(AnotherSimpleBean.class,
                                                                                   mockedFieldsRepository,
                                                                                   objectFactoryRepository);
        when(objectFactoryRepository.<Collection<AnotherSimpleBean>>getObjectFactory(eq(List.class.getName()),
                                                                                     anyString())).thenReturn(factory);
        when(objectFactoryRepository.<SimpleBean>getObjectFactory(eq(SimpleBean.class.getName()),
                                                                  anyString())).thenReturn(
                new SimpleBeanObjectFactory());
        when(objectFactoryRepository.getObjectFactory(eq(AnotherSimpleBean.class.getName()), anyString())).thenReturn(
                new FakeDefaultObjectFactory(mockedFieldsRepository));
        List<?> res = propertyManager.mockField(CTX, "collectionOfObjects", CLASS_NAME, List.class, null);
        assertNotNull(res);
        assertTrue(res.size() > 0);
        for (Object re : res) {
            assertTrue(re instanceof AnotherSimpleBean);
            assertEquals("test", ((AnotherSimpleBean) re).getTestMethod());
        }
    }

    @Test
    public void testMockByteField() throws Exception {
        insertField("byteField", "byte", "-1");
        when(objectFactoryRepository.<Number>getObjectFactory(anyString(), anyString())).thenReturn(
                new NumberFactory());
        byte res = propertyManager.mockField(CTX, "byteField", CLASS_NAME, (byte) -2);
        assertEquals(-1, res);
    }

    private void insertField(String fieldName, String fieldType, String fieldValue) {
        MockedFieldImpl field = new MockedFieldImpl();
        field.setActive(true);
        field.setClassName(CLASS_NAME);
        field.setContextPath(CTX);
        field.setFieldName(fieldName);
        field.setFieldType(fieldType);
        field.setFieldValue(fieldValue);
        when(mockedFieldsRepository.load(CTX, CLASS_NAME, fieldName)).thenReturn(field);
    }

    @Test
    public void testMockShortField() throws Exception {
        insertField("shortField", "short", "-42");
        when(objectFactoryRepository.<Number>getObjectFactory(anyString(), anyString())).thenReturn(
                new NumberFactory());
        short res = propertyManager.mockField(CTX, "shortField", CLASS_NAME, (short) -5);
        assertEquals(-42, res);
    }

    @Test
    public void testMockIntField() throws Exception {
        insertField("intField", "int", "42");
        when(objectFactoryRepository.<Number>getObjectFactory(anyString(), anyString())).thenReturn(
                new NumberFactory());
        int res = propertyManager.mockField(CTX, "intField", CLASS_NAME, -42);
        assertEquals(42, res);
    }

    @Test
    public void testMockLongField() throws Exception {
        insertField("longField", "long", "666");
        when(objectFactoryRepository.<Number>getObjectFactory(anyString(), anyString())).thenReturn(
                new NumberFactory());
        long res = propertyManager.mockField(CTX, "longField", CLASS_NAME, -666L);
        assertEquals(666L, res);
    }

    @Test
    public void testMockFloatField() throws Exception {
        insertField("floatField", "float", "42.00");
        when(objectFactoryRepository.<Number>getObjectFactory(anyString(), anyString())).thenReturn(
                new NumberFactory());
        float res = propertyManager.mockField(CTX, "floatField", CLASS_NAME, -42.0F);
        assertEquals(42.0F, res, 0.0F);
    }

    @Test
    public void testMockDoubleField() throws Exception {
        insertField("doubleField", "double", "42.00");
        when(objectFactoryRepository.<Number>getObjectFactory(anyString(), anyString())).thenReturn(
                new NumberFactory());
        double res = propertyManager.mockField(CTX, "doubleField", CLASS_NAME, -42.0D);
        assertEquals(42.0D, res, 0.0D);
    }

    @Test
    public void testMockCharField() throws Exception {
        insertField("charField", "char", new String(new char[]{'r'}));
        when(objectFactoryRepository.<Character>getObjectFactory(anyString(), anyString())).thenReturn(
                new CharacterFactory());
        char res = propertyManager.mockField(CTX, "charField", CLASS_NAME, 'a');
        assertEquals('r', res);
    }

    @Test
    public void testMockBooleanField() throws Exception {
        insertField("booleanField", "boolean", "true");
        when(objectFactoryRepository.<Boolean>getObjectFactory(anyString(), anyString())).thenReturn(
                new BooleanFactory());
        boolean res = propertyManager.mockField(CTX, "booleanField", CLASS_NAME, false);
        assertTrue(res);
    }

    @Test
    public void testMockObjectArrayField() throws Exception {
        insertField("stringArrayField", "java.lang.String[]", "one,two,three");
        when(objectFactoryRepository.<String[]>getObjectFactory(anyString(), anyString())).thenReturn(
                new ArrayFactory<>(new StringFactory()));
        String[] res = propertyManager.mockField(CTX, "stringArrayField", CLASS_NAME, String[].class, null);
        assertNotNull(res);
        assertTrue(res.length == 3);
        assertEquals("one", res[0]);
        assertEquals("two", res[1]);
        assertEquals("three", res[2]);
    }

    @Test
    public void testMockObjectEnumField() throws Exception {
        insertField("enumField", "java.lang.annotation.ElementType", "METHOD");
        when(objectFactoryRepository.<Enum<ElementType>>getObjectFactory(anyString(), anyString())).thenReturn(
                new EnumFactory<>());
        ElementType res = propertyManager.mockField(CTX, "enumField", CLASS_NAME, ElementType.class, null);
        assertNotNull(res);
        assertSame(ElementType.METHOD, res);
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
        public Class<?> getTargetClass() {
            return SimpleBean.class;
        }

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
