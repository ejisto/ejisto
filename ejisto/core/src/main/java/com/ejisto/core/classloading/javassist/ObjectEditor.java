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

package com.ejisto.core.classloading.javassist;

import javassist.CannotCompileException;
import javassist.NotFoundException;
import javassist.expr.*;
import org.apache.log4j.Logger;

import static com.ejisto.constants.StringConstants.EJISTO_CLASS_TRANSFORMER_CATEGORY;

public class ObjectEditor extends ExprEditor {
    private static final Logger logger = Logger.getLogger(EJISTO_CLASS_TRANSFORMER_CATEGORY.getValue());
    private EjistoMethodFilter filter;

    public ObjectEditor(EjistoMethodFilter filter) {
        super();
        this.filter = filter;
    }

    @Override
    public void edit(NewExpr e) throws CannotCompileException {
        super.edit(e);
    }

    @Override
    public void edit(NewArray a) throws CannotCompileException {
        super.edit(a);
    }

    @Override
    public void edit(MethodCall m) throws CannotCompileException {
        super.edit(m);
    }

    @Override
    public void edit(ConstructorCall c) throws CannotCompileException {
        super.edit(c);
    }

    /**
     * Calls <code>public static T mockField(String contextPath, String fieldName, String className, T actual)</code> where <code>T</code> is field type
     */
    @Override
    public void edit(FieldAccess f) throws CannotCompileException {
        trace("checking field [" + f.getFieldName() + "] of class [" + f.getClassName() + "]");
        if (f.isReader() && filter.isFieldHandled(f.getFieldName())) {
            trace("editing field [" + f.getFieldName() + "]");
            StringBuilder instruction = new StringBuilder("{ $_ = ($r) com.ejisto.core.classloading.javassist.PropertyManager.mockField(");
            instruction.append("\"").append(filter.getContextPath()).append("\",");
            try {
                if (f.getField().getType().isPrimitive())
                    instruction.append("\"").append(f.getFieldName()).append("\",").append("\"").append(f.getClassName()).append(
                            "\", $_); return $_; }");
                else instruction.append("\"").append(f.getFieldName()).append("\",").append("\"").append(f.getClassName()).append(
                        "\", $type, $_); return $_; }");
                trace("modifying field access with expression [" + instruction.toString() + "]");
                f.replace(instruction.toString());
                trace("done");
            } catch (NotFoundException e) {
                e.printStackTrace();
            }

        }
    }

    @Override
    public void edit(Instanceof i) throws CannotCompileException {
        super.edit(i);
    }

    @Override
    public void edit(Cast c) throws CannotCompileException {
        super.edit(c);
    }

    @Override
    public void edit(Handler h) throws CannotCompileException {
        super.edit(h);
    }

    private void trace(String s) {
        if (logger.isTraceEnabled()) logger.trace(s);
    }

}
