package com.ejisto.modules.validation;

import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import com.ejisto.modules.dao.entities.MockedField;

public class MockedFieldValidator implements Validator {

	public MockedFieldValidator() {
	}
	
	@Override
	public boolean supports(Class<?> clazz) {
		return MockedField.class.isAssignableFrom(clazz);
	}

	@Override
	public void validate(Object target, Errors errors) {
		// TODO Auto-generated method stub
	}
	
}
