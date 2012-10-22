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

package com.ejisto.modules.web.util;

import com.ejisto.core.classloading.decorator.MockedFieldDecorator;
import com.ejisto.modules.dao.entities.MockedField;
import com.ejisto.modules.dao.entities.MockedFieldImpl;
import com.ejisto.modules.web.MockedFieldRequest;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static com.ejisto.modules.web.MockedFieldRequest.requestSingleField;
import static org.junit.Assert.*;

/**
 * Created by IntelliJ IDEA.
 * User: celestino
 * Date: 6/27/12
 * Time: 11:18 AM
 */
public class JSONUtilTest {
    @Test
    public void testEncodeMockedFieldRequest() throws Exception {
        MockedFieldRequest request = requestSingleField("/ejisto-test", "com.test.TestClass", "name");
        String result = JSONUtil.encode(request);
        assertNotNull(result);
        assertEquals("{\"contextPath\":\"/ejisto-test\",\"className\":\"com.test.TestClass\",\"fieldName\":\"name\"}",
                     result);
    }

    @Test
    public void testDecodeMockedFieldRequest() throws Exception {
        String requestBody = "{\"contextPath\":\"/ejisto-test\",\"className\":\"com.test.TestClass\",\"fieldName\":\"name\"}";
        MockedFieldRequest request = JSONUtil.decode(requestBody, MockedFieldRequest.class);
        assertNotNull(request);
        assertEquals(request.getClassName(), "com.test.TestClass");
        assertEquals(request.getContextPath(), "/ejisto-test");
        assertEquals(request.getFieldName(), "name");
    }

    @Test
    public void testEncodeDecodeMockedField() throws Exception {
        MockedField mf = new MockedFieldImpl();
        mf.setActive(true);
        mf.setClassName("com.test.TestClass");
        mf.setContextPath("/ejisto-test");
        mf.setFieldName("name");
        mf.setFieldValue("Pippo Baudo");
        mf.setFieldType("java.lang.String");
        List<MockedField> input = Arrays.asList(mf);
        String encoded = JSONUtil.encodeMockedFields(input);
        assertNotNull(encoded);
        List<MockedField> output = JSONUtil.decodeMockedFields(encoded);
        assertNotNull(output);
        assertTrue(output.size() == input.size());
        for (MockedField mockedField : output) {
            assertTrue(MockedFieldDecorator.class.isInstance(mockedField));
            assertEquals("com.test.TestClass", mockedField.getClassName());
            assertEquals("/ejisto-test", mockedField.getContextPath());
            assertEquals("name", mockedField.getFieldName());
            assertEquals("Pippo Baudo", mockedField.getFieldValue());
            assertEquals("java.lang.String", mockedField.getFieldType());
        }
    }
}
