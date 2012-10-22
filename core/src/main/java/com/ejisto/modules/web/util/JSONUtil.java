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

import ch.lambdaj.function.convert.Converter;
import com.ejisto.core.classloading.decorator.MockedFieldDecorator;
import com.ejisto.modules.dao.entities.MockedField;
import com.ejisto.modules.dao.entities.MockedFieldImpl;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.util.CollectionUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.List;

import static ch.lambdaj.Lambda.*;
import static java.nio.charset.Charset.forName;
import static java.util.Collections.emptyList;

/**
 * Created by IntelliJ IDEA.
 * User: celestino
 * Date: 6/26/12
 * Time: 5:16 PM
 */
public abstract class JSONUtil {

    public static <T> String encode(T object) {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            ObjectMapper mapper = new ObjectMapper();
            mapper.writeValue(out, object);
            return new String(out.toByteArray(), forName("UTF-8"));
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    public static <T> T decode(String httpRequestBody, Class<T> objectClass) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(httpRequestBody, objectClass);
        } catch (IOException e) {
            throw new IllegalArgumentException("httpRequestBody is not a valid JSON", e);
        }
    }

    public static <T> T decode(String httpRequestBody, TypeReference<T> typeReference) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(httpRequestBody, typeReference);
        } catch (IOException e) {
            throw new IllegalArgumentException("httpRequestBody is not a valid JSON", e);
        }
    }

    public static String encodeMockedFields(Collection<MockedField> mockedFields) {
        try {
            List<? extends MockedField> unwrapped;
            if (CollectionUtils.isEmpty(mockedFields)) {
                unwrapped = emptyList();
            } else {
                unwrapped = collect(forEach(mockedFields).unwrap());
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
            return convert(fields, new Converter<Object, MockedField>() {
                @Override
                public MockedField convert(Object from) {
                    return new MockedFieldDecorator((MockedField) from);
                }
            });
        } catch (IOException e) {
            throw new IllegalArgumentException("invalid input", e);
        }
    }
}
