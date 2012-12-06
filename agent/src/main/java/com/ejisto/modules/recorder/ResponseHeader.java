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

package com.ejisto.modules.recorder;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.io.Serializable;
import java.util.Comparator;
import java.util.Date;

/**
 * Created by IntelliJ IDEA.
 * User: celestino
 * Date: 9/6/12
 * Time: 8:23 AM
 */
public class ResponseHeader implements Serializable {
    public static final long serialVersionUID = 1L;

    enum Type {
        DATE, INT, STRING
    }

    private final String name;
    private final String valueAsString;
    private final Type type;

    @JsonCreator
    public ResponseHeader(@JsonProperty("name") String name,
                          @JsonProperty("valueAsString") String valueAsString,
                          @JsonProperty("type") Type type) {
        this.name = name;
        this.valueAsString = valueAsString;
        this.type = type;
    }

    public String getStringValue() {
        return valueAsString;
    }

    public Date getDateValue() {
        if (type == Type.DATE) {
            return new Date(Long.parseLong(valueAsString));
        }
        throw new UnsupportedOperationException("type is not DATE");
    }

    public int getIntValue() {
        if (type == Type.INT) {
            return Integer.parseInt(valueAsString);
        }
        throw new UnsupportedOperationException("type is not INT");
    }

    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !obj.getClass().isAssignableFrom(getClass())) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        ResponseHeader other = (ResponseHeader) obj;
        return new EqualsBuilder().appendSuper(super.equals(obj))
                .append(valueAsString, other.valueAsString)
                .append(type, other.type).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(valueAsString).append(type).toHashCode();
    }

    public static final Comparator<ResponseHeader> COMPARATOR = new Comparator<ResponseHeader>() {
        @Override
        public int compare(ResponseHeader o1, ResponseHeader o2) {
            if (o1 == null ^ o2 == null) {
                return o1 == null ? 1 : -1;
            }
            return o1 != null ? o1.getName().compareTo(o2.getName()) : 1;
        }
    };
}
