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

import lombok.Getter;
import lombok.Setter;

/**
 * Created by IntelliJ IDEA.
 * User: celestino
 * Date: 8/5/14
 * Time: 6:38 PM
 */
@Getter
@Setter
public class TemporaryWebApplicationDescriptor extends WebApplicationDescriptor {

    private String originalContextPath;
    private String id;

    @Override
    public String getKey() {
        return originalContextPath;
    }

    public static String generateId(TemporaryWebApplicationDescriptor descriptor) {
        return String.format("%s-%s", descriptor.getId(), descriptor.getOriginalContextPath());
    }
}
