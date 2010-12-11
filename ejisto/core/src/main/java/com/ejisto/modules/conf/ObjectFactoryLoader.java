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

package com.ejisto.modules.conf;

import com.ejisto.constants.StringConstants;
import com.ejisto.core.classloading.SharedClassLoader;
import com.ejisto.modules.factory.ObjectFactory;
import com.ejisto.modules.factory.impl.*;
import com.ejisto.modules.repository.ObjectFactoryRepository;
import com.ejisto.util.FileExtensionFilter;
import javassist.ClassPool;
import javassist.CtClass;
import org.apache.log4j.Logger;

import javax.annotation.Resource;
import java.io.File;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.TimerTask;

import static com.ejisto.util.IOUtils.findAllClassesInJarFile;

/**
 * Created by IntelliJ IDEA.
 * User: celestino
 * Date: 12/11/10
 * Time: 4:57 PM
 */
public class ObjectFactoryLoader extends TimerTask {
    private static final Logger logger = Logger.getLogger(ObjectFactoryLoader.class);
    private boolean initialized = false;
    @Resource
    private ObjectFactoryRepository objectFactoryRepository;
    @Resource
    private SharedClassLoader sharedClassLoader;
    private Set<String> loadedPaths = new HashSet<String>();
    private File directory;
    private ClassPool cp;
    private CtClass bazeClazz;

    @Override
    public void run() {
        if (!directory.exists()) {
            logger.warn("directory " + directory.getAbsolutePath() + " does not exists. Exiting");
            return;
        }
        if (!initialized) init();
        for (File file : directory.listFiles(new FileExtensionFilter(FileExtensionFilter.ALL_JARS, false))) {
            try {
                processFile(file);
                loadedPaths.add(file.getAbsolutePath());
            } catch (Exception e) {
                logger.error("exception during ObjectFactory loading", e);
            }
        }
    }

    private void processFile(File file) throws Exception {
        if (loadedPaths.contains(file.getAbsolutePath())) return;
        logger.info("processing file: " + file.getAbsolutePath());
        cp.appendClassPath(file.getAbsolutePath());
        sharedClassLoader.addEntry(file.getAbsolutePath());
        CtClass clazz;
        Collection<String> clazzNames = findAllClassesInJarFile(file);
        for (String clazzName : clazzNames) {
            clazz = cp.get(clazzName);
            if (clazz.subtypeOf(bazeClazz)) {
                objectFactoryRepository.registerObjectFactory((ObjectFactory<?>) clazz.toClass().newInstance());
            }
            clazz.detach();
        }
    }

    public void init() {
        try {
            objectFactoryRepository.registerObjectFactory(new AtomicIntegerObjectFactory(), false);
            objectFactoryRepository.registerObjectFactory(new AtomicLongObjectFactory(), false);
            objectFactoryRepository.registerObjectFactory(new NumberObjectFactory(), false);
            objectFactoryRepository.registerObjectFactory(new StringObjectFactory(), false);
            objectFactoryRepository.registerObjectFactory(new DefaultObjectFactory(), false);
            directory = new File(System.getProperty(StringConstants.EXTENSIONS_DIR.getValue()));
            cp = new ClassPool(ClassPool.getDefault());
            bazeClazz = cp.get(ObjectFactory.class.getName());
            initialized = true;
        } catch (Exception e) {
            throw new ExceptionInInitializerError(e);
        }
    }
}
