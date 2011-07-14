/*
 * Ejisto, a powerful developer assistant
 *
 * Copyright (C) 2010-2011  Celestino Bellone
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

package com.ejisto.modules.cargo;

/**
 * Created by IntelliJ IDEA.
 * User: celestino
 * Date: 3/6/11
 * Time: 11:15 AM
 */
public class NotInstalledException extends Exception {

    private static final long serialVersionUID = -5951107157019492003L;
    private String id;

    public NotInstalledException(String id, Throwable cause) {
        this(id);
        initCause(cause);
    }

    public NotInstalledException(String id) {
        super(id);
        this.id = id;
    }

    public String getId() {
        return id;
    }

}
