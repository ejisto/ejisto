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

import java.util.concurrent.Callable;

/**
 * Created by IntelliJ IDEA.
 * User: celestino
 * Date: 4/2/11
 * Time: 6:57 PM
 */
public class Task<T> implements Callable<T> {

    private Callable<T> target;
    private String description;

    public Task(Callable<T> target, String description) {
        this.target = target;
        this.description = description;
    }

    @Override
    public T call() throws Exception {
        return target.call();
    }

    public String getDescription() {
        return description;
    }

}
