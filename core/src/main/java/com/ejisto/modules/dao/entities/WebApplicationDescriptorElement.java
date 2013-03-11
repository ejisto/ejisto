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

package com.ejisto.modules.dao.entities;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.io.Serializable;

@Data
public class WebApplicationDescriptorElement implements Serializable {
    enum Kind {
        CLASSPATH,
        BLACKLISTED
    }

    private static final long serialVersionUID = 1L;

    private final String path;
    private Kind kind = Kind.CLASSPATH;

    @JsonCreator
    public WebApplicationDescriptorElement(@JsonProperty("path") String path) {
        this.path = path;
    }

    public String getKind() {
        return kind.name();
    }

    @JsonIgnore
    public boolean isBlacklisted() {
        return kind == Kind.BLACKLISTED;
    }

    public void setKind(Kind kind) {
        this.kind = kind;
    }

    public void blacklist() {
        setKind(Kind.BLACKLISTED);
    }

    public void whitelist() {
        setKind(Kind.CLASSPATH);
    }
}
