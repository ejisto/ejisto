/*
 * Ejisto, a powerful developer assistant
 *
 * Copyright (C) 2010  Celestino Bellone
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

package com.ejisto.core.classloading;

import com.ejisto.core.classloading.javassist.EjistoMethodFilter;
import com.ejisto.core.classloading.javassist.EjistoMethodHandler;
import com.ejisto.core.classloading.javassist.ObjectEditor;
import com.ejisto.modules.dao.entities.MockedField;
import javassist.ClassClassPath;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.LoaderClassPath;
import javassist.bytecode.ClassFile;
import javassist.util.proxy.ProxyFactory;
import org.apache.log4j.Logger;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;
import java.util.List;

import static com.ejisto.util.SpringBridge.getMockedFieldsFor;

public class ClassTransformer implements ClassFileTransformer {

	private static final Logger logger = Logger.getLogger(ClassTransformer.class);
	private EjistoClassLoader classLoader;
	private ClassPool classPool;
	private String contextPath;
	
	public ClassTransformer(EjistoClassLoader classLoader) {
		this.classLoader = classLoader;
		this.contextPath = classLoader.getContextPath();
		initClassPool();
	}
	
	private void initClassPool() {
		classPool = new ClassPool();
		classPool.appendClassPath(new LoaderClassPath(classLoader));
	}
	
	public Class<?> transform(String className) throws Exception {
		CtClass clazz = classPool.get(className.replaceAll("/", "."));
		clazz.instrument(new ObjectEditor(new EjistoMethodFilter(contextPath, getFieldsFor(className))));
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
	    return getMockedFieldsFor(contextPath, className);
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
		proxyFactory.setFilter(new EjistoMethodFilter(contextPath, fields));
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
