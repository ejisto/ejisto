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

package com.ejisto.modules.repository;

import com.ejisto.event.EventManager;
import com.ejisto.event.def.StatusBarMessage;
import javassist.ClassPool;
import javassist.CtClass;
import org.apache.log4j.Logger;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: celestino
 * Date: 12/11/10
 * Time: 4:41 PM
 */
public class ObjectFactoryRepository {
    private static final Logger logger = Logger.getLogger(ObjectFactoryRepository.class);
    private static final String DEFAULT = "java.lang.Object";
    private static final ObjectFactoryRepository INSTANCE = new ObjectFactoryRepository();
    private Map<String, String> factories = new HashMap<String, String>();

    @Resource
    private EventManager eventManager;

    public static ObjectFactoryRepository getInstance() {
        return INSTANCE;
    }

    private ObjectFactoryRepository() {
        registerObjectFactory("com.ejisto.modules.factory.impl.AtomicIntegerFactory",
                              "java.util.concurrent.AtomicInteger", false);
        registerObjectFactory("com.ejisto.modules.factory.impl.AtomicLongFactory",
                              "java.util.concurrent.atomic.AtomicLong", false);
        registerObjectFactory("com.ejisto.modules.factory.impl.NumberFactory", "java.lang.Number", false);
        registerObjectFactory("com.ejisto.modules.factory.impl.StringFactory", "java.lang.String", false);
        registerObjectFactory("com.ejisto.modules.factory.impl.DefaultObjectFactory", DEFAULT, false);
        registerObjectFactory("com.ejisto.modules.factory.impl.CollectionFactory", "java.util.Collection", false);
        registerObjectFactory("com.ejisto.modules.factory.impl.MapFactory", "java.util.Map", false);
    }

    public void registerObjectFactory(String objectFactoryClassName, String targetClassName) {
        registerObjectFactory(objectFactoryClassName, targetClassName, true);
    }

    public void registerObjectFactory(String objectFactoryClassName, String targetClassName, boolean notify) {
        String message;
        boolean error = factories.containsKey(targetClassName);
        if (!error) {
            factories.put(targetClassName, objectFactoryClassName);
            message = "registered ObjectFactory [" + objectFactoryClassName + "] for class " + targetClassName;
        } else {
            message = "rejected ObjectFactory [" + objectFactoryClassName + "]";
        }
        if (notify && eventManager != null) eventManager.publishEvent(new StatusBarMessage(this, message, error));
    }

    public String getObjectFactory(String objectClassName, String contextPath) {
        try {
            return scanForObjectFactory(retrieveClassPool(contextPath).get(objectClassName));
        } catch (Exception e) {
            logger.error("getObjectFactory failed with exception, returning default one", e);
            return factories.get(DEFAULT);
        }
    }

    private ClassPool retrieveClassPool(String contextPath) {
        return ClassPoolRepository.getRegisteredClassPool(contextPath);
    }

    private String scanForObjectFactory(CtClass objectClass) throws Exception {
        try {
            debug("Hey! Could someone create an instance of [" + objectClass.getName() + "]?");
            if (factories.containsKey(objectClass.getName())) {
                debug("yep!");
                return factories.get(objectClass.getName());
            }
            String factory;
            debug("nope. Trying interfaces...");
            for (CtClass c : objectClass.getInterfaces()) {
                factory = scanForObjectFactory(c);
                if (factory != null) return factory;
            }
            if (!objectClass.isInterface()) {
                debug("nope. Trying super class...");
                factory = scanForObjectFactory(objectClass.getSuperclass());
                if (factory != null) return factory;
                return factories.get(DEFAULT);
            }
            return null;
        } finally {
            objectClass.detach();
        }
    }

    private void debug(String message) {
        if (logger.isDebugEnabled()) logger.debug(message);
    }

}
