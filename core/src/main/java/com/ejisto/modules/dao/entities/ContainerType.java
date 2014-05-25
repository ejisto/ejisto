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

package com.ejisto.modules.dao.entities;

import com.ejisto.util.ContainerTypeDeserializer;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import java.util.Arrays;

/**
 * Created by IntelliJ IDEA.
 * User: celestino
 * Date: 5/21/14
 * Time: 7:13 PM
 */
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
@JsonDeserialize(using = ContainerTypeDeserializer.class)
public enum ContainerType {
    TOMCAT_8("tomcat8x", "Apache Tomcat 8.x");

    private final String cargoID;
    private final String name;

    private ContainerType(String cargoID, String name) {
        this.cargoID = cargoID;
        this.name = name;
    }

    public String getCargoID() {
        return cargoID;
    }

    public String getName() {
        return name;
    }

    public static ContainerType fromCargoId(String cargoId) {
        return Arrays.stream(values()).filter(t -> t.getCargoID().equals(cargoId))
                .findFirst()
                .orElseThrow(IllegalArgumentException::new);
    }
}
