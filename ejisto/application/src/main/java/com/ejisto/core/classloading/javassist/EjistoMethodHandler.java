package com.ejisto.core.classloading.javassist;

import static ch.lambdaj.Lambda.having;
import static ch.lambdaj.Lambda.on;
import static ch.lambdaj.Lambda.selectFirst;
import static org.hamcrest.Matchers.equalTo;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Collection;

import javassist.util.proxy.MethodHandler;

import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import com.ejisto.modules.dao.entities.MockedField;

public class EjistoMethodHandler implements MethodHandler {
	
	private Collection<MockedField> fields;

	public EjistoMethodHandler(Collection<MockedField> fields) {
		this.fields=fields;
	}

	@Override
	public Object invoke(Object self, Method thisMethod, Method proceed,
			Object[] args) throws Throwable {
		return getFieldValue(thisMethod);
	}
	
	private Object getFieldValue(Method method) throws Exception {
		MockedField mockedField = retrieveFieldToMock(method.getName());
		Assert.notNull(mockedField);
		String storedMethod = "get"+StringUtils.capitalize(mockedField.getFieldName());
		Assert.isTrue(method.getName().equals(storedMethod), "method mismatch: declared method ["+method.getName()+"], stored method ["+storedMethod+"]");
		Class<?> returnType = method.getReturnType();
		Constructor<?> constructor = returnType.getConstructor(String.class);
		return constructor.newInstance(mockedField.getFieldValue());
	}
	
	private MockedField retrieveFieldToMock(String methodName) {
		return selectFirst(fields, having(on(MockedField.class).getFieldName(), equalTo(StringUtils.uncapitalize(methodName.substring(4)))));
	}

}
