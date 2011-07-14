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

package com.ejisto.modules.repository;

import javassist.ClassPool;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: celestino
 * Date: Dec 5, 2010
 * Time: 12:19:16 PM
 */
public class ClassPoolRepository {
    private static final ClassPoolRepository INSTANCE = new ClassPoolRepository();
    private final Map<String, ClassPool> dictionary;

    public static void registerClassPool(String context, ClassPool cp) {
        INSTANCE.putValue(context, cp);
    }

    public static ClassPool getRegisteredClassPool(String context) {
        return INSTANCE.getValue(context);
    }

    public static ClassPoolRepository getInstance() {
        return INSTANCE;
    }

    private ClassPoolRepository() {
        dictionary = Collections.synchronizedMap(new HashMap<String, ClassPool>());
    }

    private void putValue(String context, ClassPool cp) {
        dictionary.put(context, cp);
    }

    private ClassPool getValue(String context) {
        return dictionary.get(context);
    }

}
