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
