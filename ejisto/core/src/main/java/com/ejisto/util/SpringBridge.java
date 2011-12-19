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

package com.ejisto.util;

import com.ejisto.core.classloading.SharedClassLoader;
import com.ejisto.event.EventManager;
import com.ejisto.modules.cargo.NotInstalledException;
import com.ejisto.modules.dao.entities.Container;
import com.ejisto.modules.repository.ContainersRepository;
import org.springframework.context.*;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

public class SpringBridge implements ApplicationContextAware {
    private static SpringBridge instance = new SpringBridge();
    private ApplicationContext applicationContext;
    @Resource private MessageSource messageSource;
    @Resource private SharedClassLoader sharedClassLoader;

    public static boolean publishApplicationEvent(ApplicationEvent e) {
        boolean ret = isApplicationInitialized();
        if (ret) getInstance().getBean("eventManager", EventManager.class).publishEvent(e);
        return ret;
    }

    static String getMessage(String key, String language, Object... values) {
        if (!isApplicationInitialized()) return "!!Application not initialized!!";
        return getInstance().internalGetMessage(key, language, values);
    }

//    public static Class<?> loadClassFromSharedClassLoader(String name) {
//        if (!isApplicationInitialized()) return null;
//        return getInstance().loadClass(name);
//    }

    public static void addExtraPathToSharedClassLoader(String path) {
        getInstance().addExtraPath(path);
    }

    public static void addExtraPathsToSharedClassLoader(Collection<String> paths) {
        getInstance().addExtraPaths(paths);
    }

    public static List<Container> loadExistingContainers() {
        return getInstance().getBean("containersRepository", ContainersRepository.class).loadContainers();
    }

    public static Container loadExistingContainer(String id) throws NotInstalledException {
        return getInstance().getBean("containersRepository", ContainersRepository.class).loadDefault();
    }

    public static SpringBridge getInstance() {
        return instance;
    }

    private static boolean isApplicationInitialized() {
        return getInstance().applicationContext != null;
    }

    private SpringBridge() {
    }

    public <T> T getBean(String name, Class<T> type) {
        if (this.applicationContext != null) return this.applicationContext.getBean(name, type);
        else return null;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    private String internalGetMessage(String key, String language, Object... values) {
        try {
            return messageSource.getMessage(key, values, new Locale(language));
        } catch (NoSuchMessageException e) {
            return key;
        }
    }

//    private Class<?> loadClass(String name) {
//        return sharedClassLoader.loadClass(name);
//    }

    private void addExtraPath(String path) {
        sharedClassLoader.addEntry(path);
    }

    private void addExtraPaths(Collection<String> paths) {
        sharedClassLoader.addEntries(paths);
    }

}
