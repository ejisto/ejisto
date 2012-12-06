/*
 * Ejisto, a powerful developer assistant
 *
 * Copyright (C) 2010-2012  Celestino Bellone
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

import ch.lambdaj.group.Group;
import com.ejisto.core.ApplicationException;
import com.ejisto.core.classloading.ClassTransformer;
import com.ejisto.modules.dao.entities.MockedField;
import com.ejisto.modules.dao.entities.WebApplicationDescriptor;
import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.NotFoundException;
import lombok.extern.log4j.Log4j;
import org.apache.commons.io.FilenameUtils;
import org.springframework.util.CollectionUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.RecursiveAction;

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
    private final List<Group<MockedField>> groupedFields;

    public ScanAction(WebApplicationDescriptor descriptor, List<Group<MockedField>> groupedFields) {
        this.descriptor = descriptor;
        this.groupedFields = groupedFields;
    }

    @Override
    protected void compute() {
        if (CollectionUtils.isEmpty(groupedFields)) {
            log.debug("done. Exiting");
            return;
        }

        List<Group<MockedField>> toBeScanned;
        List<Group<MockedField>> toBeForked;
        if (groupedFields.size() > SIZE_THRESHOLD) {
            log.debug("forking...");
            toBeScanned = groupedFields.subList(0, SIZE_THRESHOLD);
            toBeForked = groupedFields.subList(SIZE_THRESHOLD, groupedFields.size());
        } else {
            toBeScanned = new ArrayList<>(groupedFields);
            toBeForked = null;
        }
        invokeAll(new ScanAction(descriptor, toBeForked));
        scanGroups(toBeScanned, descriptor);
    }

    private static void scanGroups(List<Group<MockedField>> groups, WebApplicationDescriptor descriptor) {
        try {
            ClassPool classPool = new ClassPool();
            String webInf = FilenameUtils.normalizeNoEndSeparator(
                    descriptor.getDeployablePath()) + File.separator + "WEB-INF";
            classPool.appendClassPath(webInf + File.separator + "classes");
            classPool.appendClassPath(webInf + File.separator + "lib/*");
            classPool.appendSystemPath();
            ClassTransformer transformer = new ClassTransformer(descriptor.getContextPath());
            for (Group<MockedField> group : groups) {
                scanClass(group, classPool, transformer, normalize(webInf + File.separator + "classes/", true));
            }
        } catch (Exception e) {
            log.error("got exception: " + e.toString());
            throw new ApplicationException(e);
        }
    }

    private static void scanClass(Group<MockedField> group, ClassPool classPool, ClassTransformer transformer, String destPath)
            throws NotFoundException, CannotCompileException, IOException {
        MockedField head = group.first();
        log.debug("scanning " + head.getClassName());
        CtClass clazz = classPool.get(head.getClassName());
        transformer.addMissingProperties(clazz, group.findAll());
        clazz.writeFile(destPath);
    }
}
