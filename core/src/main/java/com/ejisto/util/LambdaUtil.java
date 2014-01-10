/*
 * Ejisto, a powerful developer assistant
 *
 * Copyright (C) 2010-2014 Celestino Bellone
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

package com.ejisto.util;

import com.ejisto.modules.dao.entities.MockedField;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Created by IntelliJ IDEA.
 * User: celestino
 * Date: 1/8/14
 * Time: 5:21 PM
 */
public final class LambdaUtil {
    private LambdaUtil() {}

    public static Predicate<MockedField> findFieldByName(String fieldName) {
        return (f -> f.getFieldName().equals(fieldName));
    }

    private static Predicate<MockedField> findFieldByContextPath(String contextPath) {
        return (f -> f.getContextPath().equals(contextPath));
    }

    private static Predicate<MockedField> findFieldByClassName(String className) {
        return (f -> f.getClassName().equals(className));
    }

    public static Predicate<MockedField> findFieldByContextPathAndClassName(String contextPath, String className) {
        return findFieldByContextPath(contextPath).and(findFieldByClassName(className));
    }

    public static Predicate<MockedField> findField(String contextPath, String className, String fieldName) {
        return findFieldByContextPath(contextPath)
                .and(findFieldByClassName(className))
                .and(findFieldByName(fieldName));
    }

    public static Predicate<File> isDirectory() {
        return (File::isDirectory);
    }

    public static <T extends ActionListener> Consumer<ActionEvent> callActionPerformed(T instance) {
        return instance::actionPerformed;
    }

}
