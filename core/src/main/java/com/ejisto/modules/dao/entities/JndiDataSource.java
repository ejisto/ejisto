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

import lombok.Data;

@Data
public class JndiDataSource {
    private long id;
    private String name;
    private String type;
    private int maxActive;
    private int maxIdle;
    private long maxWait;
    private String username;
    private String password;
    private String driverClassName;
    private String url;
    private String driverJarPath;
    private boolean alreadyBound;

    @Override
    public String toString() {
        return getName();
    }
}
