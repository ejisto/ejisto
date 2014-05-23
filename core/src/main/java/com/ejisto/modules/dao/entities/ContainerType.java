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

import java.util.Arrays;

/**
 * Created by IntelliJ IDEA.
 * User: celestino
 * Date: 5/21/14
 * Time: 7:13 PM
 */
public enum ContainerType {
    TOMCAT_8("tomcat8x", "Apache Tomcat 8.x");

    private final String cargoId;
    private final String name;

    ContainerType(String cargoId, String name) {
        this.cargoId = cargoId;
        this.name = name;
    }

    public String getCargoId() {
        return cargoId;
    }

    public String getName() {
        return name;
    }

    public static ContainerType fromCargoId(String cargoId) {
        return Arrays.stream(values()).filter(t -> t.getCargoId().equals(cargoId))
                .findFirst()
                .orElseThrow(IllegalArgumentException::new);
    }
}
