/*
 * Ejisto, a powerful developer assistant
 *
 * Copyright (C) 2011  Celestino Bellone
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
import com.ejisto.modules.dao.CustomObjectFactoryDao;
import com.ejisto.modules.factory.ObjectFactory;
import com.ejisto.modules.factory.impl.*;
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
    private Map<String, ObjectFactory<?>> factories = new HashMap<String, ObjectFactory<?>>();
    @Resource
    private EventManager eventManager;

    public static ObjectFactoryRepository getInstance() {
        return INSTANCE;
    }

    private ObjectFactoryRepository() {
        registerObjectFactory(new AtomicIntegerFactory(), false);
        registerObjectFactory(new AtomicLongFactory(), false);
        registerObjectFactory(new NumberFactory(), false);
        registerObjectFactory(new StringFactory(), false);
        registerObjectFactory(new DefaultObjectFactory(), false);
        registerObjectFactory(new CollectionFactory(), false);
    }

    public void registerObjectFactory(ObjectFactory<?> objectFactory) {
        registerObjectFactory(objectFactory, true);
    }

    public void registerObjectFactory(ObjectFactory<?> objectFactory, boolean notify) {
        String message;
        boolean error = factories.containsKey(objectFactory.getTargetClassName());
        if (!error) {
            factories.put(objectFactory.getTargetClassName(), objectFactory);
            message = "loaded ObjectFactory [" + objectFactory.getClass().getName() + "] for class " + objectFactory.getTargetClassName();
        } else {
            message = "rejected ObjectFactory [" + objectFactory.getClass().getName() + "]";
        }
        if (notify) eventManager.publishEvent(new StatusBarMessage(this, message, error));
    }
    
    @SuppressWarnings("unchecked")
    public <T> ObjectFactory<T> getObjectFactory(String objectClassName) {
        try {
            return (ObjectFactory<T>) getObjectFactory(Class.forName(objectClassName));
        } catch (Exception e) {
            logger.error("getObjectFactory failed with exception, returning default one", e);
            return (ObjectFactory<T>) factories.get(DEFAULT);
        }
    }

    @SuppressWarnings("unchecked")
    public <T> ObjectFactory<T> getObjectFactory(Class<T> objectClass) {
        try {
            if (factories.containsKey(objectClass.getName()))
                return (ObjectFactory<T>) factories.get(objectClass.getName());
            return (ObjectFactory<T>) scanForObjectFactory(ClassPool.getDefault().get(objectClass.getName()));
        } catch (Exception e) {
            logger.error("getObjectFactory failed with exception, returning default one", e);
            return (ObjectFactory<T>) factories.get(DEFAULT);
        }
    }

    private ObjectFactory<?> scanForObjectFactory(CtClass objectClass) throws Exception {
        try {
            debug("Hey! Could someone create an instance of [" + objectClass.getName() + "]?");
            if (factories.containsKey(objectClass.getName())) {
                debug("yep!");
                return factories.get(objectClass.getName());
            }
            debug("nope. Trying interfaces...");
            ObjectFactory<?> factory;
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
