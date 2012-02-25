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

package com.ejisto.hello.beans;

import lombok.Data;

import java.io.Serializable;
import java.util.Collection;

@Data
public class HelloWorldBean implements Serializable {

    private static final long serialVersionUID = 6175502203990776213L;
    private String title = "Unchanged Title";
    private String description = "Unchanged Description";
    private Collection<SimplePropertyValue> propertyValues;
    private boolean displayHeader;

    private int hits = 0;
    private long timestamp = 0L;

}
