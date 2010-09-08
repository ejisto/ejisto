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

package com.ejisto.core.classloading;

import static ch.lambdaj.Lambda.having;
import static ch.lambdaj.Lambda.on;
import static ch.lambdaj.Lambda.select;
import static org.hamcrest.Matchers.equalTo;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;
import java.util.Collection;
import java.util.List;

import javassist.ClassClassPath;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.LoaderClassPath;
import javassist.bytecode.ClassFile;
import javassist.util.proxy.ProxyFactory;

import org.apache.log4j.Logger;

import com.ejisto.core.classloading.javassist.EjistoMethodFilter;
import com.ejisto.core.classloading.javassist.EjistoMethodHandler;
import com.ejisto.core.classloading.javassist.ObjectEditor;
import com.ejisto.modules.dao.entities.MockedField;

public class ClassTransformer implements ClassFileTransformer {

	private static final Logger logger = Logger.getLogger(ClassTransformer.class);
	private Collection<MockedField> mockedFields;
	private EjistoClassLoader classLoader;
	private ClassPool classPool;
	
	public ClassTransformer(EjistoClassLoader classLoader, Collection<MockedField> mockedFields) {
		this.mockedFields = mockedFields;
		this.classLoader = classLoader;
		initClassPool();
		logger.info("Created ClassTransformer for fields: "+mockedFields);
	}
	
	private void initClassPool() {
		classPool = new ClassPool();
		classPool.appendClassPath(new LoaderClassPath(classLoader));
	}
	
	public Class<?> transform(String className) throws Exception {
		CtClass clazz = classPool.get(className.replaceAll("/", "."));
		clazz.instrument(new ObjectEditor(new EjistoMethodFilter(getFieldsFor(className))));
		Class<?> transformedClass = clazz.toClass();
		clazz.detach();
		return transformedClass;
	}

	@Override
	public byte[] transform(ClassLoader loader, String className,
			Class<?> classBeingRedefined, ProtectionDomain protectionDomain,
			byte[] classfileBuffer) throws IllegalClassFormatException {
		if(loader == null || !classLoader.getClass().isAssignableFrom(loader.getClass())) {
			if(logger.isTraceEnabled()) logger.trace("no match for classloader "+loader+ " while trying to transform class "+className);
			return null;
		}
		
		List<MockedField> fields = getFieldsFor(className);
		if(fields == null || fields.isEmpty()) return null;
		else return proxyClass(className, classfileBuffer, fields);
	}
	
	private List<MockedField> getFieldsFor(String className) {
		return select(mockedFields, having(on(MockedField.class).getClassName(), equalTo(className.replaceAll("/", "."))));
	}
	
	private byte[] proxyClass(String className, byte[] classFileBuffer, List<MockedField> fields) {
		try {
			String canonicalName = className.replaceAll("/", ".");
			byte[] newArray = new byte[classFileBuffer.length];
			System.arraycopy(classFileBuffer, 0, newArray, 0, classFileBuffer.length);
			ClassPool cp = new ClassPool();
			ClassFile cf = new ClassFile(new DataInputStream(new ByteArrayInputStream(newArray)));
			return addMagic(canonicalName, cf, cp, fields);
		} catch (Exception e) {
			logger.error("unable to instrument class", e);
			throw new RuntimeException(e);
		}
	}
	
	private byte[] addMagic(String className, ClassFile clazz, ClassPool cp, List<MockedField> fields) throws Exception {
		ProxyFactory proxyFactory = new ProxyFactory();
		proxyFactory.setInterfaces(getInterfaces(clazz.getInterfaces()));
		proxyFactory.setSuperclass(classLoader.loadInstrumentableClass(className));
		proxyFactory.setFilter(new EjistoMethodFilter(fields));
		proxyFactory.setHandler(new EjistoMethodHandler(fields));
		Class<?> proxy = proxyFactory.createClass();
		cp.appendClassPath(new ClassClassPath(proxy));
		return cp.get(className).toBytecode();
	}
	
	private Class<?>[] getInterfaces(String[] classes) throws ClassNotFoundException {
		Class<?>[] interfaces = new Class<?>[classes.length];
		for (int i=0; i<classes.length; i++) {
			interfaces[i] = Class.forName(classes[i]);
		}
		return interfaces;
	}
	
}
