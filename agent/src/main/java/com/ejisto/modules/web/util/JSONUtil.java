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

import com.fasterxml.jackson.annotation.JsonIgnoreType;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static java.nio.charset.Charset.forName;

/**
 * Created by IntelliJ IDEA.
 * User: celestino
 * Date: 6/26/12
 * Time: 5:16 PM
 */
public abstract class JSONUtil {

    public static <T> String encode(T object, Class<?>... typeToIgnore) {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            ObjectMapper mapper = new ObjectMapper();
            mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
            for (Class<?> aClass : typeToIgnore) {
                mapper.addMixInAnnotations(aClass, Ignorable.class);
            }
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

    @JsonIgnoreType
    private static abstract class Ignorable {
    }
}
