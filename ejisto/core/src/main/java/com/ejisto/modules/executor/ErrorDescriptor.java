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

package com.ejisto.modules.executor;

/**
 * Created by IntelliJ IDEA.
 * User: celestino
 * Date: 12/13/11
 * Time: 7:03 PM
 */
public class ErrorDescriptor {
    private final Throwable error;
    private final String category;

    public ErrorDescriptor(Throwable error, String category) {
        this.error = error;
        this.category = category;
    }

    public Throwable getError() {
        return error;
    }

    public String getErrorDescription() {
        return error.toString();
    }

    public String getCategory() {
        return category;
    }
}
