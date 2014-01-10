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

package com.ejisto.modules.web.util;

import com.ejisto.core.classloading.decorator.MockedFieldDecorator;
import com.ejisto.modules.dao.entities.MockedField;
import com.ejisto.modules.dao.entities.MockedFieldImpl;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.List;

import static java.nio.charset.Charset.forName;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;

/**
 * Created by IntelliJ IDEA.
 * User: celestino
 * Date: 11/5/12
 * Time: 8:24 AM
 */
public abstract class MockedFieldsJSONUtil {

    public static String encodeMockedFields(Collection<MockedField> mockedFields) {
        try {
            List<? extends MockedField> unwrapped;
            if (mockedFields == null || mockedFields.isEmpty()) {
                unwrapped = emptyList();
            } else {
                unwrapped = mockedFields.stream().map(MockedField::unwrap).collect(toList());
            }
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            ObjectMapper mapper = new ObjectMapper();
            mapper.writeValue(out, unwrapped);
            return new String(out.toByteArray(), forName("UTF-8"));
        } catch (IOException e) {
            throw new IllegalArgumentException("invalid input", e);
        }
    }

    public static List<MockedField> decodeMockedFields(String httpRequestBody) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            List<MockedFieldImpl> fields = mapper.readValue(httpRequestBody,
                                                            new TypeReference<List<MockedFieldImpl>>() {
                                                            });
            return fields.stream().map(f -> new MockedFieldDecorator((MockedFieldImpl)f)).collect(toList());
        } catch (IOException e) {
            throw new IllegalArgumentException("invalid input", e);
        }
    }
}
