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

package com.ejisto.core.classloading;

import com.ejisto.core.classloading.javassist.EjistoMethodFilter;
import com.ejisto.core.classloading.javassist.ObjectEditor;
import com.ejisto.modules.dao.entities.MockedField;
import com.ejisto.modules.repository.MockedFieldsRepository;
import javassist.*;
import org.apache.log4j.Logger;
import org.springframework.util.CollectionUtils;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;
import java.util.List;

import static com.ejisto.constants.StringConstants.EJISTO_CLASS_TRANSFORMER_CATEGORY;
import static java.lang.Thread.currentThread;

public class ClassTransformer implements ClassFileTransformer {

    private static final Logger logger = Logger.getLogger(EJISTO_CLASS_TRANSFORMER_CATEGORY.getValue());
    //    private EjistoClassLoader classLoader;
    private ClassPool classPool;
    private String contextPath;

    public ClassTransformer(String contextPath) {
        this.contextPath = contextPath;
        initClassPool();
    }

    private void initClassPool() {
        classPool = new ClassPool();
        classPool.appendClassPath(new LoaderClassPath(currentThread().getContextClassLoader()));
    }

    public Class<?> transform(String className) throws Exception {
        CtClass clazz = instrument(className);
        Class<?> transformedClass = clazz.toClass();
        clazz.detach();
        return transformedClass;
    }

    private CtClass instrument(String className) throws Exception {
        CtClass clazz = classPool.get(className.replaceAll("/", "."));
        if (clazz.isFrozen()) clazz.defrost();
        removeFinalModifier(clazz);
        addDefaultConstructor(clazz);
        clazz.instrument(new ObjectEditor(new EjistoMethodFilter(contextPath, getFieldsFor(className))));
        return clazz;
    }

    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
        if (!isInstrumentableClass(className)) return null;
        trace(className + " is instrumentable. Loading fields...");
        List<MockedField> fields = getFieldsFor(getCanonicalClassName(className));
        trace(className + " has registered fields: " + !CollectionUtils.isEmpty(fields));
        if (CollectionUtils.isEmpty(fields)) return null;
        else return transform(className, fields);
    }

    private void removeFinalModifier(CtClass clazz) {
        int modifiers = clazz.getModifiers();
        if (Modifier.isFinal(clazz.getModifiers())) {
            int cleanModifiers = Modifier.clear(modifiers, Modifier.FINAL);
            clazz.setModifiers(cleanModifiers);
        }
    }

    private void addDefaultConstructor(CtClass clazz) throws NotFoundException, CannotCompileException {
        boolean found = false;
        for (CtConstructor constructor : clazz.getConstructors()) {
            if (constructor.getParameterTypes().length == 0) {
                found = true;
                break;
            }
        }
        if (!found) clazz.addConstructor(new CtConstructor(new CtClass[0], clazz));
    }

    private byte[] transform(String className, List<MockedField> mockedFields) throws IllegalClassFormatException {
        try {
            trace("retrieving " + className + " from pool");
            CtClass clazz = classPool.get(getCanonicalClassName(className));
            trace("instrumenting " + className);
            clazz.instrument(new ObjectEditor(new EjistoMethodFilter(contextPath, mockedFields)));
            trace("removing final modifier (if present)");
            removeFinalModifier(clazz);
            trace("adding default constructor ");
            addDefaultConstructor(clazz);
            trace("done. Returning bytecode");
            clazz.rebuildClassFile();
            return clazz.toBytecode();
        } catch (Exception e) {
            logger.error("error during transformation of class " + className, e);
            throw new IllegalClassFormatException(e.getMessage());
        }
    }

    private List<MockedField> getFieldsFor(String className) {
        return MockedFieldsRepository.getInstance().load(contextPath, className);
    }

    private String getCanonicalClassName(String path) {
        return path.replaceAll("/", ".");
    }

    public boolean isInstrumentableClass(String name) {
        return MockedFieldsRepository.getInstance().isMockableClass(contextPath, getCanonicalClassName(name));
    }

    private void trace(String s) {
        if (logger.isTraceEnabled()) logger.trace(s);
    }
}
