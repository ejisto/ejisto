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

package com.ejisto.core.classloading.javassist;

import javassist.CannotCompileException;
import javassist.CtClass;
import javassist.CtField;
import javassist.NotFoundException;
import javassist.expr.ExprEditor;
import javassist.expr.FieldAccess;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import java.util.Arrays;
import java.util.Optional;

import static com.ejisto.constants.StringConstants.EJISTO_CLASS_TRANSFORMER_CATEGORY;
import static java.lang.String.format;

public class ObjectEditor extends ExprEditor {
    private static final Logger logger = Logger.getLogger(EJISTO_CLASS_TRANSFORMER_CATEGORY.getValue());
    private static final String PREFIX = "ejisto$$";
    private final EjistoMethodFilter filter;


    public ObjectEditor(EjistoMethodFilter filter) {
        super();
        this.filter = filter;
    }

    /**
     * Edits a field access replacing the code
     * with a call to {@link PropertyManager#mockField(String, String, String, Class, Object)}}.
     *
     * @param f the "fieldAccess"
     */
    @Override
    public void edit(FieldAccess f) throws CannotCompileException {
        String fieldName = f.getFieldName();
        if (shouldBeEdited(f, fieldName) && editFieldReader(f)) {
            CtClass clazz = f.getEnclosingClass();
            final Optional<String> fieldMarker = getFieldMarker(fieldName);
            if(fieldMarker.isPresent()) {
                clazz.addField(CtField.make(format("private boolean %s = true;", fieldMarker.get()), clazz));
            }
        }
    }

    private boolean shouldBeEdited(FieldAccess f, String fieldName) {
        return !fieldName.startsWith(PREFIX) && f.isReader() && filter.isFieldHandled(fieldName);
    }

    private boolean editFieldReader(FieldAccess f) throws CannotCompileException {
        String fieldName = f.getFieldName();
        if (!f.where().getMethodInfo().isMethod()) {
            trace("skipping field [" + fieldName + "] because current context is either a constructor or a static initializer");
            return false;
        }
        if(hasAlreadyBeenProcessed(f, fieldName)) {
            trace("skipping field [" + fieldName + "] because it has already been processed");
            return false;
        }
        trace("editing field [" + fieldName + "]");
        StringBuilder instruction = new StringBuilder(
                "{ $_ = $proceed($$); $_ = ($r) com.ejisto.core.classloading.javassist.PropertyManager#newRemoteInstance().mockField(");
        instruction.append("\"").append(filter.getContextPath()).append("\",");
        try {
            CtField ctField = f.getEnclosingClass().getDeclaredField(fieldName);
            if (ctField.getType().isPrimitive()) {
                instruction.append("\"").append(fieldName).append("\",").append("\"").append(
                        f.getClassName()).append(
                        "\", $_); }");
            } else {
                instruction.append("\"").append(fieldName).append("\",").append("\"").append(
                        f.getClassName()).append(
                        "\", $type, $_); }");
            }
            trace("modifying field access with expression [" + instruction.toString() + "]");
            f.replace(instruction.toString());
            trace("done");
            return true;
        } catch (NotFoundException e) {
            logger.error("not found error", e);
        }
        return false;
    }

    private void trace(String s) {
        if (logger.isTraceEnabled()) {
            logger.trace(s);
        }
    }

    private static Optional<String> getFieldMarker(String fieldName) {
        if(StringUtils.isBlank(fieldName)) {
            return Optional.empty();
        }
        return Optional.of(PREFIX + fieldName);
    }

    private static boolean hasAlreadyBeenProcessed(FieldAccess f, String fieldName) {
        Optional<String> fieldMarker = getFieldMarker(fieldName);
        return !fieldMarker.isPresent() || Arrays.stream(f.getEnclosingClass().getDeclaredFields())
                .anyMatch(field -> field.getName().equals(fieldMarker.get()));
    }
}
