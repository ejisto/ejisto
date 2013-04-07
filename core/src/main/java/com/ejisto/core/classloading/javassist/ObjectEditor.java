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
import javassist.NotFoundException;
import javassist.expr.ExprEditor;
import javassist.expr.FieldAccess;
import org.apache.log4j.Logger;

import static com.ejisto.constants.StringConstants.EJISTO_CLASS_TRANSFORMER_CATEGORY;

public class ObjectEditor extends ExprEditor {
    private static final Logger logger = Logger.getLogger(EJISTO_CLASS_TRANSFORMER_CATEGORY.getValue());
    private final EjistoMethodFilter filter;


    public ObjectEditor(EjistoMethodFilter filter) {
        super();
        this.filter = filter;
    }

    /**
     * Edits a field access replacing the code with a call to {@link PropertyManager#mockField(String, String, String, Class, Object)}}.
     * If session recording mode is enabled, it also records write attempts.
     *
     * @param f the "fieldAccess"
     */
    @Override
    public void edit(FieldAccess f) throws CannotCompileException {
        trace("checking field [" + f.getFieldName() + "] of class [" + f.getClassName() + "]");
        if (f.isReader() && filter.isFieldHandled(f.getFieldName())) {
            editFieldReader(f);
        }
    }

    private void editFieldReader(FieldAccess f) throws CannotCompileException {
        if (!f.where().getMethodInfo().isMethod()) {
            trace("skipping field [" + f.getFieldName() + "] because current context is either constructor or static initializer");
            return;
        }
        trace("editing field [" + f.getFieldName() + "]");
        StringBuilder instruction = new StringBuilder(
                "{ $_ = $proceed($$); $_ = ($r) com.ejisto.core.classloading.javassist.PropertyManager#newRemoteInstance().mockField(");
        instruction.append("\"").append(filter.getContextPath()).append("\",");
        try {
            if (f.getField().getType().isPrimitive()) {
                instruction.append("\"").append(f.getFieldName()).append("\",").append("\"").append(
                        f.getClassName()).append(
                        "\", $_); }");
            } else {
                instruction.append("\"").append(f.getFieldName()).append("\",").append("\"").append(
                        f.getClassName()).append(
                        "\", $type, $_); }");
            }
            trace("modifying field access with expression [" + instruction.toString() + "]");
            f.replace(instruction.toString());
            trace("done");
        } catch (NotFoundException e) {
            logger.error("not found error", e);
        }
    }

    private void trace(String s) {
        if (logger.isTraceEnabled()) {
            logger.trace(s);
        }
    }
}
