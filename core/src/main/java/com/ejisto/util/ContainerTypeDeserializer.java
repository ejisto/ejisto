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

package com.ejisto.util;

import com.ejisto.modules.dao.entities.ContainerType;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import java.io.IOException;
import java.util.Optional;

/**
 * Created by IntelliJ IDEA.
 * User: celestino
 * Date: 5/25/14
 * Time: 10:07 AM
 */
public class ContainerTypeDeserializer extends StdDeserializer<ContainerType> {

    public ContainerTypeDeserializer() {
        super(ContainerType.class);
    }

    @Override
    public ContainerType deserialize(JsonParser jp, DeserializationContext ctx) throws IOException {
        ContainerType value = null;
        while(true) {
            if(jp.getCurrentToken() == JsonToken.FIELD_NAME && "cargoID".equals(jp.getText())) {
                value = ContainerType.fromCargoId(jp.nextTextValue());
            }
            if(jp.nextToken() == JsonToken.END_OBJECT) {
                break;
            }
        }
        return Optional.ofNullable(value).orElseThrow(IOException::new);
    }
}
