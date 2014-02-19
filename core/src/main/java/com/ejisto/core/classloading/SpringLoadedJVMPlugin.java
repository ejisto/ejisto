/*
 * Ejisto, a powerful developer assistant
 *
 * Copyright (C) 2010-2014 Celestino Bellone
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

import com.ejisto.sl.ClassTransformer;
import org.apache.log4j.Logger;
import org.springsource.loaded.LoadtimeInstrumentationPlugin;
import org.springsource.loaded.ReloadEventProcessorPlugin;

import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;

import static com.ejisto.constants.StringConstants.EJISTO_CLASS_TRANSFORMER_CATEGORY;

/**
 * Created by IntelliJ IDEA.
 * User: celestino
 * Date: 2/11/14
 * Time: 8:28 AM
 */
public class SpringLoadedJVMPlugin implements ReloadEventProcessorPlugin, LoadtimeInstrumentationPlugin {
    private static final Logger LOGGER = Logger.getLogger(EJISTO_CLASS_TRANSFORMER_CATEGORY.getValue());
    private static volatile ClassTransformer transformer;

    public SpringLoadedJVMPlugin() {
        LOGGER.info("SpringLoadedJVMPlugin : called constructor");
    }

    @Override
    public boolean accept(String slashedTypeName, ClassLoader classLoader, ProtectionDomain protectionDomain, byte[] bytes) {
        return initCompleted() && slashedTypeName != null &&
                transformer.isInstrumentableClass(toDottedClassName(slashedTypeName));
    }

    @Override
    public byte[] modify(String slashedClassName, ClassLoader classLoader, byte[] bytes) {
        try {
            return transformer.transform(classLoader, toDottedClassName(slashedClassName), null, null, bytes);
        } catch (IllegalClassFormatException e) {
            LOGGER.warn(String.format("unable to transform class %s", slashedClassName), e);
            return null;
        }
    }

    @Override
    public boolean shouldRerunStaticInitializer(String typename, Class<?> clazz, String encodedTimestamp) {
        return false;
    }

    @Override
    public void reloadEvent(String typename, Class<?> clazz, String encodedTimestamp) {
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info(String.format("%s has been reloaded", typename));
        }
    }

    public static void initClassTransformer(ClassTransformer classTransformer) {
        if(classTransformer != null) {
            LOGGER.info("ClassTransformer init complete");
            transformer = classTransformer;
        }
    }

    public static void disableTransformer() {
        transformer = null;
    }

    private static boolean initCompleted() {
        return transformer != null;
    }

    private static String toDottedClassName(String slashedClassName) {
        return slashedClassName.replaceAll("/", ".");
    }
}
