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

package com.ejisto.modules.cargo.util;

/**
 * Created by IntelliJ IDEA.
 * User: celestino
 * Date: 2/26/12
 * Time: 6:43 PM
 */
public class DownloadTimeout extends RuntimeException {
    private static final long serialVersionUID = -4771893347922750361L;

    public DownloadTimeout(String message) {
        super(message);
    }

    public DownloadTimeout(String message, Throwable cause) {
        super(message, cause);
    }
}
