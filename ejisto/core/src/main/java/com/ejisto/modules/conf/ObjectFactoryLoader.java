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

package com.ejisto.modules.conf;

import com.ejisto.constants.StringConstants;
import com.ejisto.modules.dao.entities.CustomObjectFactory;
import com.ejisto.modules.factory.ObjectFactory;
import com.ejisto.modules.repository.ObjectFactoryRepository;
import com.ejisto.util.FileExtensionFilter;
import javassist.ClassPool;
import javassist.CtClass;
import lombok.extern.log4j.Log4j;
import org.apache.commons.codec.digest.DigestUtils;

import javax.annotation.Resource;
import java.io.File;
import java.io.FileInputStream;
import java.util.Collection;

import static com.ejisto.util.IOUtils.findAllClassesInJarFile;

/**
 * Created by IntelliJ IDEA.
 * User: celestino
 * Date: 12/11/10
 * Time: 4:57 PM
 */
@Log4j
public class ObjectFactoryLoader implements Runnable {
    private boolean initialized = false;
    @Resource private ObjectFactoryRepository objectFactoryRepository;
    @Resource private com.ejisto.modules.dao.CustomObjectFactoryDao customObjectFactoryDao;
    private File directory;
    private ClassPool cp;
    private CtClass bazeClazz;

    @Override
    public void run() {
        if (!initialized) {
            init();
        }
        if (!initialized) {
            return; //workaround to avoid startup failures.
        }
        if (!directory.exists()) {
            log.warn("directory " + directory.getAbsolutePath() + " does not exist. Exiting");
            return;
        }
        for (File file : directory.listFiles(new FileExtensionFilter(FileExtensionFilter.ALL_JARS, false))) {
            try {
                processFile(file);
            } catch (Exception e) {
                log.error("exception during ObjectFactory loading", e);
            }
        }
    }

    private void processFile(File file) throws Exception {
        CustomObjectFactory factory = customObjectFactoryDao.load(file.getName());
        String checksum = DigestUtils.shaHex(new FileInputStream(file));
        if (factory != null && factory.getChecksum().equals(checksum)) {
            return;
        }
        log.info("processing file: " + file.getAbsolutePath());
        cp.appendClassPath(file.getAbsolutePath());
        CtClass clazz;
        Collection<String> clazzNames = findAllClassesInJarFile(file);
        ObjectFactory<?> factoryInstance;
        for (String clazzName : clazzNames) {
            clazz = cp.get(clazzName);
            if (clazz.subtypeOf(bazeClazz)) {
                factoryInstance = (ObjectFactory<?>) clazz.toClass().newInstance();
                objectFactoryRepository.registerObjectFactory(clazz.getName(), factoryInstance.getTargetClassName());
            }
            clazz.detach();
        }
        saveCustomObjectFactory(factory, file, checksum);
    }

    private void saveCustomObjectFactory(CustomObjectFactory factory, File file, String checksum) {
        if (factory == null) {
            factory = new CustomObjectFactory();
        }
        factory.setFileName(file.getName());
        factory.setProcessed(true);
        factory.setChecksum(checksum);
        customObjectFactoryDao.save(factory);
    }

    public void init() {
        try {
            if (System.getProperty(StringConstants.EXTENSIONS_DIR.getValue()) == null) {
                return;
            }
            directory = new File(System.getProperty(StringConstants.EXTENSIONS_DIR.getValue()));
            cp = new ClassPool(ClassPool.getDefault());
            bazeClazz = cp.get(ObjectFactory.class.getName());
            initialized = true;
        } catch (Exception e) {
            throw new ExceptionInInitializerError(e);
        }
    }
}
