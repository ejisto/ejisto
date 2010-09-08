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

import java.lang.reflect.Constructor;

import javax.annotation.Resource;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

import com.ejisto.modules.dao.MockedFieldsDao;
import com.ejisto.modules.dao.entities.MockedField;

public class PropertyManager implements InitializingBean {

	@Resource
	private MockedFieldsDao mockedFieldsDao;

	private static PropertyManager INSTANCE;

	private <T> T getFieldValue(String className, String fieldName,	Class<T> type) {
		try {
			MockedField mockedField = mockedFieldsDao.getMockedField(className, fieldName);
			Assert.notNull(mockedField);
			Constructor<T> constructor = type.getConstructor(String.class);
			return constructor.newInstance(mockedField.getFieldValue());
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		INSTANCE = this;
	}
	
	public static <T> T mockField(String fieldName, String className,
			Class<T> type) {
		return INSTANCE.getFieldValue(className, fieldName, type);
	}

	public static int mockField(String fieldName, String className,
			int actual) {
		return INSTANCE.getFieldValue(className, fieldName, Integer.class);
	}
	
	public static long mockField(String fieldName, String className,
			long actual) {
		return INSTANCE.getFieldValue(className, fieldName, Long.class);
	}
	
	public static double mockField(String fieldName, String className,
			double actual) {
		return INSTANCE.getFieldValue(className, fieldName, Double.class);
	}
	
	public static float mockField(String fieldName, String className,
			float actual) {
		return INSTANCE.getFieldValue(className, fieldName, Float.class);
	}
	
	public static short mockField(String fieldName, String className,
			short actual) {
		return INSTANCE.getFieldValue(className, fieldName, Short.class);
	}
	
	public static byte mockField(String fieldName, String className,
			byte actual) {
		return INSTANCE.getFieldValue(className, fieldName, Byte.class);
	}
}
