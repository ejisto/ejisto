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

package com.ejisto.modules.repository;

import javassist.ClassPool;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Created by IntelliJ IDEA.
 * User: celestino
 * Date: Dec 5, 2010
 * Time: 12:19:16 PM
 */
public final class ClassPoolRepository {
    private static final ClassPoolRepository INSTANCE = new ClassPoolRepository();
    private final ConcurrentMap<String, ClassPool> classPoolContainer;

    public static ClassPool getRegisteredClassPool(String context) {
        return INSTANCE.getValue(context);
    }

    public static void replaceClassPool(String context, ClassPool classPool) {
        final ClassPool existing = INSTANCE.getValue(context);
        INSTANCE.classPoolContainer.replace(context, existing, classPool);
    }

    public static ClassPoolRepository getInstance() {
        return INSTANCE;
    }

    private ClassPoolRepository() {
        classPoolContainer = new ConcurrentHashMap<>();
    }

    private ClassPool getValue(String context) {
        classPoolContainer.computeIfAbsent(context, ctx -> new ClassPool(ClassPool.getDefault()));
        return classPoolContainer.get(context);
    }

}
