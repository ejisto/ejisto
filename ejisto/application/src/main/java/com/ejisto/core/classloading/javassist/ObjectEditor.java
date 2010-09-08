/*******************************************************************************
 * Copyright 2010 Celestino Bellone
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package com.ejisto.core.classloading.javassist;

import javassist.CannotCompileException;
import javassist.NotFoundException;
import javassist.expr.Cast;
import javassist.expr.ConstructorCall;
import javassist.expr.ExprEditor;
import javassist.expr.FieldAccess;
import javassist.expr.Handler;
import javassist.expr.Instanceof;
import javassist.expr.MethodCall;
import javassist.expr.NewArray;
import javassist.expr.NewExpr;

public class ObjectEditor extends ExprEditor {
	
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
	 * Calls <code>public static T mockField(String fieldName, String className, T actual)</code> where <code>T</code> is field type
	 */
	@Override
	public void edit(FieldAccess f) throws CannotCompileException {
		if(f.isReader() && filter.isFieldHandled(f.getFieldName())) {
			StringBuilder instruction = new StringBuilder("{ $_ = ($r) com.ejisto.core.classloading.javassist.PropertyManager.mockField(");
			try {
				if(f.getField().getType().isPrimitive())
					instruction.append("\"").append(f.getFieldName()).append("\",").append("\"").append(f.getClassName()).append("\", $_); return $_; }");
				else
					instruction.append("\"").append(f.getFieldName()).append("\",").append("\"").append(f.getClassName()).append("\", $type); return $_; }");
				f.replace(instruction.toString());
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
	
	

}
