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

package com.ejisto.core.classloading.util;

import java.util.ArrayList;

public class AutoGrowingList<T> extends ArrayList<T> {
    private static final long serialVersionUID = -2813154819230798444L;

    @Override
    public T get(int index) {
        ensureCapacity(index + 1);
        return super.get(index);
    }

    @Override
    public T set(int index, T element) {
        ensureCapacity(index + 1);
        return super.set(index, element);
    }

    @Override
    public void add(int index, T element) {
        ensureCapacity(index + 1);
        super.add(index, element);
    }
}
