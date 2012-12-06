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

/**
 * Simple but meaningful immutable Object.
 * <p/>
 * Created by IntelliJ IDEA.
 * User: celestino
 * Date: 8/24/12
 * Time: 8:21 AM
 */
public class ImmutableObject {

    private final String finalString;
    private final boolean finalBoolean;
    private final int finalInt;

    public ImmutableObject(String finalString, boolean finalBoolean, int finalInt) {
        this.finalString = finalString;
        this.finalBoolean = finalBoolean;
        this.finalInt = finalInt;
    }

    public String getFinalString() {
        return finalString;
    }

    public boolean isFinalBoolean() {
        return finalBoolean;
    }

    public int getFinalInt() {
        return finalInt;
    }
}
