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

import static ch.lambdaj.Lambda.having;
import static ch.lambdaj.Lambda.on;
import static ch.lambdaj.Lambda.selectFirst;
import static org.hamcrest.Matchers.equalTo;

import java.lang.reflect.Method;
import java.util.Collection;

import javassist.util.proxy.MethodFilter;

import org.springframework.util.StringUtils;

import com.ejisto.modules.dao.entities.MockedField;

public class EjistoMethodFilter implements MethodFilter {
	
	private Collection<MockedField> fields;

	public EjistoMethodFilter(Collection<MockedField> fields) {
		this.fields=fields;
	}

	@Override
	public boolean isHandled(Method m) {
		String methodName = m.getName();
		return methodName.startsWith("get") && isFieldHandled(methodName.substring(4));
	}
	
	public boolean isFieldHandled(String fieldName) {
		return selectFirst(fields, having(on(MockedField.class).getFieldName(),equalTo(StringUtils.uncapitalize(fieldName)))) != null;
	}

}
