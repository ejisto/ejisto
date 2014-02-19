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

package com.ejisto.core.classloading.scan;

import com.ejisto.core.ApplicationException;
import com.ejisto.core.classloading.ClassTransformerImpl;
import com.ejisto.modules.dao.entities.MockedField;
import com.ejisto.modules.dao.entities.WebApplicationDescriptor;
import com.ejisto.modules.repository.MockedFieldsRepository;
import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.NotFoundException;
import lombok.extern.log4j.Log4j;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.RecursiveAction;

import static java.util.stream.Collectors.toMap;
import static org.apache.commons.io.FilenameUtils.normalize;

/**
 * Created by IntelliJ IDEA.
 * User: celestino
 * Date: 8/2/12
 * Time: 8:18 AM
 */
@Log4j
public final class ScanAction extends RecursiveAction {
    private static final int SIZE_THRESHOLD = 25;
    private final WebApplicationDescriptor descriptor;
    private final Map<String, List<MockedField>> groupedFields;
    private final MockedFieldsRepository mockedFieldsRepository;

    public ScanAction(WebApplicationDescriptor descriptor,
                      Map<String, List<MockedField>> groupedFields,
                      MockedFieldsRepository mockedFieldsRepository) {
        this.descriptor = descriptor;
        this.groupedFields = groupedFields;
        this.mockedFieldsRepository = mockedFieldsRepository;
    }

    @Override
    protected void compute() {
        if (groupedFields.isEmpty()) {
            log.debug("done. Exiting");
            return;
        }
        int classesSize = groupedFields.size();
        Map<String,List<MockedField>> toBeScanned;
        Map<String,List<MockedField>> toBeForked;
        if (classesSize > SIZE_THRESHOLD) {
            log.debug("forking...");
            toBeScanned = groupedFields.entrySet().stream()
                    .limit(SIZE_THRESHOLD)
                    .collect(toMap(Map.Entry::getKey, Map.Entry::getValue));
            toBeForked = groupedFields.entrySet().stream()
                    .filter(e -> !toBeScanned.containsKey(e.getKey()))
                    .collect(toMap(Map.Entry::getKey, Map.Entry::getValue));
        } else {
            toBeScanned = groupedFields;
            toBeForked = Collections.emptyMap();
        }
        invokeAll(new ScanAction(descriptor, toBeForked, mockedFieldsRepository));
        scanGroups(toBeScanned, descriptor);
    }

    private void scanGroups(Map<String, List<MockedField>> groups, WebApplicationDescriptor descriptor) {
        try {
            ClassPool classPool = new ClassPool();
            String webInf = FilenameUtils.normalizeNoEndSeparator(
                    descriptor.getDeployablePath()) + File.separator + "WEB-INF";
            classPool.appendClassPath(webInf + File.separator + "classes");
            classPool.appendClassPath(webInf + File.separator + "lib/*");
            classPool.appendSystemPath();
            ClassTransformerImpl transformer = new ClassTransformerImpl(descriptor.getContextPath(), mockedFieldsRepository, null);
            groups.forEach((k, v) -> scanClass(v, classPool, transformer, normalize(webInf + File.separator + "classes/", true)));
        } catch (Exception e) {
            log.error("got exception: " + e.toString());
            throw new ApplicationException(e);
        }
    }

    private static void scanClass(List<MockedField> group, ClassPool classPool, ClassTransformerImpl transformer, String destPath) {
        if(!group.isEmpty()) {
            MockedField head = group.get(0);
            log.debug("scanning " + head.getClassName());
            try {
                CtClass clazz = classPool.get(head.getClassName());
                transformer.addMissingProperties(clazz, group);
                clazz.writeFile(destPath);
            } catch (NotFoundException | CannotCompileException | IOException e) {
                log.error("got exception: " + e.toString());
                throw new ApplicationException(e);
            }
        }
    }
}
