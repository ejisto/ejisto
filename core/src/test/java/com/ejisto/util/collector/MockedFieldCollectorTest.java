/*
 * Ejisto, a powerful developer assistant
 *
 * Copyright (C) 2010-2014 Celestino Bellone
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

package com.ejisto.util.collector;

import com.ejisto.modules.dao.entities.MockedField;
import com.ejisto.modules.dao.entities.MockedFieldImpl;
import org.apache.commons.collections4.ListUtils;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static org.junit.Assert.*;

/**
 * Created by IntelliJ IDEA.
 * User: celestino
 * Date: 4/1/14
 * Time: 7:56 AM
 */
public class MockedFieldCollectorTest {

    private List<MockedField> testClassFields = new ArrayList<>();
    private List<MockedField> test2ClassFields = new ArrayList<>();
    private List<MockedField> otherFields = new ArrayList<>();
    private MockedFieldCollector instance = new MockedFieldCollector();

    @Before
    public void setUp() {
        testClassFields.add(createField("com.ejisto.test.Test", "firstField", "/ejisto-test"));
        testClassFields.add(createField("com.ejisto.test.Test", "secondField", "/ejisto-test"));
        test2ClassFields.add(createField("com.ejisto.test2.Test", "thirdField", "/ejisto-test"));
        otherFields.add(createField("com.ejisto.test.Test", "thirdField", "/another-one"));
        otherFields.add(createField("com.ejisto.test.Test", "fourthField", "/another-one"));
    }

    private MockedField createField(String className, String fieldName, String contextPath) {
        MockedField mf = new MockedFieldImpl();
        mf.setClassName(className);
        mf.setFieldName(fieldName);
        mf.setContextPath(contextPath);
        return mf;
    }

    @Test
    public void testCombiner() throws Exception {
        Map<String, List<MockedField>> first = new TreeMap<>();
        Map<String, List<MockedField>> second = new TreeMap<>();
        first.put("com.ejisto.test.Test", testClassFields);
        first.put("com.ejisto.test2.Test", test2ClassFields);
        second.put("com.ejisto.test.Test", otherFields);
        Map<String, List<MockedField>> result = instance.combiner().apply(first, second);
        assertTrue(result.containsKey("com.ejisto.test.Test"));
        assertTrue(result.containsKey("com.ejisto.test2.Test"));
        assertEquals(testClassFields.size() + otherFields.size(), result.get("com.ejisto.test.Test").size());
    }

    @Test
    public void testFinisher() throws Exception {
        Map<String, List<MockedField>> map = new TreeMap<>();
        map.put("com.ejisto.test.Test", ListUtils.union(testClassFields, otherFields));
        map.put("com.ejisto.test2.Test", test2ClassFields);
        final FieldNode result = instance.finisher().apply(map);
        assertNotNull(result);
        assertTrue(result.isRoot());
        assertFalse(result.getChildren().isEmpty());
        assertEquals(2, result.getChildren().size()); // /ejisto-test and /another-one
        FieldNode anotherOne = result.getChildren().first();
        assertNotNull(anotherOne);
        assertEquals("/another-one", anotherOne.getLabel());
        assertFalse(anotherOne.getChildren().isEmpty());
        assertEquals(1, anotherOne.getChildren().size());
        FieldNode ejistoTest = result.getChildren().last();
        assertEquals("/ejisto-test", ejistoTest.getLabel());
        assertFalse(ejistoTest.getChildren().isEmpty());
        assertEquals(1, ejistoTest.getChildren().size());
    }
}
