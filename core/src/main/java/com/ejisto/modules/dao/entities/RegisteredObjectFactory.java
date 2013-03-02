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

/**
 * Created by IntelliJ IDEA.
 * User: celestino
 * Date: 7/31/11
 * Time: 7:02 PM
 */
public class RegisteredObjectFactory implements Entity<String> {
    private final String className;
    private final String targetClassName;

    public RegisteredObjectFactory(String className, String targetClassName) {
        this.className = className;
        this.targetClassName = targetClassName;
    }

    public String getClassName() {
        return className;
    }

    public String getTargetClassName() {
        return targetClassName;
    }

    @Override
    public String getKey() {
        return className;
    }
}
