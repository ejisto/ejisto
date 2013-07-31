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

package com.ejisto.modules.controller.wizard.installer.workers;

import com.ejisto.core.classloading.decorator.MockedFieldDecorator;
import com.ejisto.core.classloading.util.ReflectionUtils;
import com.ejisto.modules.dao.entities.MockedField;
import com.ejisto.modules.dao.entities.WebApplicationDescriptor;
import com.ejisto.modules.executor.ErrorDescriptor;
import com.ejisto.modules.repository.MockedFieldsRepository;
import javassist.*;
import javassist.bytecode.SignatureAttribute;
import lombok.extern.log4j.Log4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.RecursiveTask;

import static com.ejisto.modules.executor.ErrorDescriptor.Category.ERROR;
import static com.ejisto.modules.executor.ErrorDescriptor.Category.WARN;
import static java.util.Collections.emptyList;

/**
 * Created by IntelliJ IDEA.
 * User: celestino
 * Date: 8/8/12
 * Time: 6:21 PM
 */
@Log4j
public class LoadClassAction extends RecursiveTask<List<MockedField>> {
    private static final int THRESHOLD = 500;
    private final List<String> classes;
    private final ClassLoader classLoader;
    private final WebApplicationDescriptor webApplicationDescriptor;
    private final ProgressListener listener;
    private final MockedFieldsRepository mockedFieldsRepository;

    public LoadClassAction(List<String> classes, ClassLoader classLoader, WebApplicationDescriptor webApplicationDescriptor, ProgressListener listener, MockedFieldsRepository mockedFieldsRepository) {
        this.classes = classes;
        this.classLoader = classLoader;
        this.webApplicationDescriptor = webApplicationDescriptor;
        this.listener = listener;
        this.mockedFieldsRepository = mockedFieldsRepository;
    }

    @Override
    protected List<MockedField> compute() {
        if (CollectionUtils.isEmpty(classes)) {
            return emptyList();
        }
        if (classes.size() > THRESHOLD) {
            return doParallelWork();
        } else {
            log.debug("attempting to load " + classes.size() + " classes.");
            List<MockedField> results = new ArrayList<>();
            ClassPool classPool = new ClassPool();
            classPool.appendClassPath(new LoaderClassPath(classLoader));
            for (String className : classes) {
                listener.progressChanged(1, className);
                results.addAll(loadClassFields(className, classPool, webApplicationDescriptor));
            }
            log.debug("returning results.");
            return results;
        }
    }

    private List<MockedField> doParallelWork() {
        int collectionSize = classes.size();
        int jobs = collectionSize / THRESHOLD + (collectionSize % THRESHOLD == 0 ? 0 : 1);
        log.debug("about to fork " + jobs + " tasks");
        List<LoadClassAction> forkedTasks = new ArrayList<>(jobs);
        List<MockedField> results = new ArrayList<>();
        for (int i = 0; i < jobs; i++) {
            int start = THRESHOLD * i;
            int end = Math.min(collectionSize, start + THRESHOLD);
            LoadClassAction task = new LoadClassAction(classes.subList(start, end), classLoader,
                                                       webApplicationDescriptor, listener, mockedFieldsRepository);
            task.fork();
            forkedTasks.add(task);
        }
        log.debug("collecting results");
        for (LoadClassAction task : forkedTasks) {
            results.addAll(task.join());
        }
        log.debug("done");
        return results;
    }

    private List<MockedField> loadClassFields(String className, ClassPool cp, WebApplicationDescriptor descriptor) {
        CtClass clazz = null;
        try {
            clazz = cp.get(className);
            return getMockedFields(clazz, descriptor);
        } catch (Exception | InternalError e) {
            listener.errorOccurred(buildErrorDescriptor(e));
        } finally {
            if (clazz != null) {
                clazz.detach();
            }
        }
        return emptyList();
    }

    private List<MockedField> getMockedFields(CtClass clazz, WebApplicationDescriptor descriptor) throws NotFoundException {
        List<MockedField> results = new ArrayList<>();
        try {
            for (CtField field : clazz.getDeclaredFields()) {
                MockedField mockedField;
                if (mockedFieldsRepository.exists(descriptor.getContextPath(), clazz.getName(),
                                                  field.getName())) {
                    MockedField existing = mockedFieldsRepository.load(descriptor.getContextPath(), clazz.getName(),
                                                                       field.getName());
                    mockedField = MockedFieldDecorator.copyOf(existing);
                } else {
                    mockedField = new MockedFieldDecorator();
                }
                mockedField.setContextPath(descriptor.getContextPath());
                mockedField.setClassName(clazz.getName());
                mockedField.setFieldName(field.getName());
                mockedField.setFieldType(getFieldTypeAsString(field));
                parseGenerics(field, mockedField);
                results.add(mockedField);
            }
            CtClass zuperclazz = clazz.getSuperclass();
            if (!zuperclazz.getName().startsWith("java")) {
                results.addAll(getMockedFields(zuperclazz, descriptor));
            }
        } catch (Exception e) {
            listener.errorOccurred(buildErrorDescriptor(e));
        }
        return results;
    }

    private String getFieldTypeAsString(CtField field) throws NotFoundException {
        CtClass type = field.getType();
        if (type.isArray()) {
            //using com.custom.Class[] notation instead of [Lcom.custom.Class; in order to improve
            //readability; standard notation is also supported.
            return type.getComponentType().getName() + "[]";
        }
        return type.getName();
    }

    private ErrorDescriptor buildErrorDescriptor(Throwable e) {
        Class<?> c = e.getClass();
        boolean classIssue = NotFoundException.class.isAssignableFrom(c) ||
                LinkageError.class.isAssignableFrom(c);
        return new ErrorDescriptor(e, classIssue ? WARN : ERROR);
    }

    private void parseGenerics(CtField field, MockedField mockedField) {
        try {
            String encodedSignature = field.getGenericSignature();
            if (StringUtils.isNotEmpty(encodedSignature)) {
                SignatureAttribute.ObjectType decoded = SignatureAttribute.toFieldSignature(encodedSignature);
                if (!SignatureAttribute.TypeVariable.class.isInstance(decoded)) {
                    String signature = decoded.toString();
                    mockedField.setFieldElementType(ReflectionUtils.cleanGenericSignature(signature));
                }
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }
}
