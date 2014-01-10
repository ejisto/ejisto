/*
 * Ejisto, a powerful developer assistant
 *
 * Copyright (C) 2010-2013 Celestino Bellone
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
import javassist.bytecode.AccessFlag;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.ejisto.constants.StringConstants.EJISTO_CLASS_TRANSFORMER_CATEGORY;
import static com.ejisto.modules.web.MockedFieldRequest.requestAllClasses;
import static java.lang.String.format;
import static java.lang.Thread.currentThread;

public class ClassTransformer implements ClassFileTransformer {

    private static final Logger logger = Logger.getLogger(EJISTO_CLASS_TRANSFORMER_CATEGORY.getValue());
    private ClassPool classPool;
    private final String contextPath;
    private final Collection<String> registeredClassNames;
    private final MockedFieldsRepository mockedFieldsRepository;

    public ClassTransformer(String contextPath, MockedFieldsRepository mockedFieldsRepository) {
        this.contextPath = contextPath;
        this.mockedFieldsRepository = mockedFieldsRepository;
        this.registeredClassNames = loadAllRegisteredClassNames(contextPath, mockedFieldsRepository);
        initClassPool();
    }

    private void initClassPool() {
        classPool = new ClassPool();
        classPool.appendClassPath(new LoaderClassPath(currentThread().getContextClassLoader()));
    }

    public Class<?> transform(String className) throws CannotCompileException, NotFoundException {
        CtClass clazz = instrument(className);
        Class<?> transformedClass = clazz.toClass();
        clazz.detach();
        return transformedClass;
    }

    private CtClass instrument(String className) throws NotFoundException, CannotCompileException {
        CtClass clazz = load(className);
        removeFinalModifier(clazz);
        addDefaultConstructor(clazz);
        List<MockedField> configuredFields = getFieldsFor(className);
        addMissingProperties(clazz, configuredFields);
        ObjectEditor editor = new ObjectEditor(new EjistoMethodFilter(contextPath, configuredFields));
        clazz.instrument(editor);
        return clazz;
    }

    public void addMissingProperties(CtClass clazz, List<MockedField> configuredFields) throws CannotCompileException, NotFoundException {
        trace("trying to add missing properties");
        for (MockedField field : configuredFields) {
            createPropertyIfNotFound(clazz, field);
        }
    }

    private void createPropertyIfNotFound(CtClass clazz, MockedField field) throws CannotCompileException, NotFoundException {
        try {
            clazz.getField(field.getFieldName());
        } catch (NotFoundException e) {
            createMissingProperty(clazz, field);
        }
    }

    private void createMissingProperty(CtClass clazz, MockedField mockedField) throws CannotCompileException, NotFoundException {
        trace("creating property " + mockedField.getFieldName());
        CtField ctField = new CtField(load(mockedField.getFieldType()), mockedField.getFieldName(), clazz);
        ctField.setModifiers(AccessFlag.PRIVATE);
        clazz.addField(ctField);
        String methodSuffix = StringUtils.capitalize(mockedField.getFieldName());
        trace("creating getter: get" + methodSuffix);
        CtMethod getter = CtNewMethod.getter("get" + methodSuffix, ctField);
        trace(format("created [%s]", getter.getSignature()));
        clazz.addMethod(getter);
        trace("creating setter...");
        CtMethod setter = CtNewMethod.setter("set" + methodSuffix, ctField);
        trace(format("created [%s]", setter.getSignature()));
        clazz.addMethod(setter);
        trace("done.");
    }

    private CtClass load(String className) throws NotFoundException {
        CtClass clazz = classPool.get(className.replaceAll("/", "."));
        if (clazz.isFrozen()) {
            clazz.defrost();
        }
        return clazz;
    }


    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
        if (!isInstrumentableClass(className)) {
            return null;
        }
        trace(className + " is instrumentable. Loading fields...");
        List<MockedField> fields = getFieldsFor(getCanonicalClassName(className));
        boolean hasFields = fields != null && !fields.isEmpty();
        trace(className + " has registered fields: " + hasFields);
        if (!hasFields) {
            return null;
        } else {
            return transform(className, fields);
        }
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
        if (!found) {
            //before adding a new empty constructor, we must scan all the fields in order to find
            //final ones. Final modifier must be removed.
            removeFinalModifierFromFields(clazz.getFields());
            CtConstructor defaultConstructor = new CtConstructor(new CtClass[0], clazz);
            defaultConstructor.setBody(null);//default constructor only calls "super()"
            clazz.addConstructor(defaultConstructor);
        }
    }

    private void removeFinalModifierFromFields(CtField[] fields) {
        for (CtField field : fields) {
            if (Modifier.isFinal(field.getModifiers())) {
                int cleanModifiers = Modifier.clear(field.getModifiers(), Modifier.FINAL);
                field.setModifiers(cleanModifiers);
            }
        }
    }

    private byte[] transform(String className, List<MockedField> mockedFields) throws IllegalClassFormatException {
        try {
            trace("retrieving " + className + " from pool");
            CtClass clazz = classPool.get(getCanonicalClassName(className));
            addMissingProperties(clazz, mockedFields);
            trace("instrumenting " + className);
            clazz.instrument(new ObjectEditor(new EjistoMethodFilter(contextPath, mockedFields)));
            trace("removing final modifier (if present)");
            removeFinalModifier(clazz);
            trace("adding default constructor, if none present ");
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
        return mockedFieldsRepository.load(contextPath, className);
    }

    private String getCanonicalClassName(String path) {
        return path.replaceAll("/", ".");
    }

    public boolean isInstrumentableClass(String name) {
        return registeredClassNames.contains(getCanonicalClassName(name));
    }

    private static void trace(String s) {
        if (logger.isTraceEnabled()) {
            logger.trace(s);
        }
    }

    private static Collection<String> loadAllRegisteredClassNames(String contextPath,
                                                                  MockedFieldsRepository mockedFieldsRepository) {
        Collection<MockedField> fields = mockedFieldsRepository.load(requestAllClasses(contextPath));
        Set<String> classes = fields.stream().map(MockedField::getClassName).collect(Collectors.toSet());
        trace(format("filtered classes for %s: %s of %s", contextPath, classes, fields));
        return classes;
    }
}
