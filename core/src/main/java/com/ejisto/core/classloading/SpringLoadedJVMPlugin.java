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
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springsource.loaded.LoadtimeInstrumentationPlugin;
import org.springsource.loaded.ReloadEventProcessorPlugin;

import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;
import java.util.Objects;
import java.util.function.Consumer;

import static com.ejisto.constants.StringConstants.EJISTO_CLASS_TRANSFORMER_CATEGORY;
import static org.apache.commons.lang3.StringUtils.indexOf;
import static org.apache.commons.lang3.StringUtils.substring;

/**
 * Created by IntelliJ IDEA.
 * User: celestino
 * Date: 2/11/14
 * Time: 8:28 AM
 */
public class SpringLoadedJVMPlugin implements ReloadEventProcessorPlugin, LoadtimeInstrumentationPlugin {
    private static final Logger LOGGER = Logger.getLogger(EJISTO_CLASS_TRANSFORMER_CATEGORY.getValue());
    private static volatile ClassTransformer transformer;
    private final Consumer<String> reloadEventHandler;

    public SpringLoadedJVMPlugin(Consumer<String> reloadEventHandler) {
        LOGGER.info("SpringLoadedJVMPlugin : called constructor");
        Objects.requireNonNull(reloadEventHandler, "reloadEventHandler cannot be null");
        this.reloadEventHandler = reloadEventHandler;
    }

    @Override
    public boolean accept(String slashedTypeName, ClassLoader classLoader, ProtectionDomain protectionDomain, byte[] bytes) {
        return initCompleted() && slashedTypeName != null &&
                transformer.isInstrumentableClass(cleanClassName(slashedTypeName));
    }

    @Override
    public byte[] modify(String slashedClassName, ClassLoader classLoader, byte[] bytes) {
        try {
            return transformer.transform(classLoader, cleanClassName(slashedClassName), null, null, bytes);
        } catch (IllegalClassFormatException e) {
            LOGGER.warn(String.format("unable to transform class %s", slashedClassName), e);
            return null;
        }
    }

    @Override
    public boolean shouldRerunStaticInitializer(String typeName, Class<?> clazz, String encodedTimestamp) {
        return false;
    }

    @Override
    public void reloadEvent(String typeName, Class<?> clazz, String encodedTimestamp) {
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info(String.format("%s has been reloaded", typeName));
        }
        transformer.resetClassPool();
        reloadEventHandler.accept(typeName);
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

    private static String cleanClassName(String slashedClassName) {
        String dottedClassName = slashedClassName.replaceAll("/", ".");
        final int dollarIndex = indexOf(dottedClassName, "$");
        if(dollarIndex > -1) {
            return substring(dottedClassName, 0, dollarIndex);
        }
        return dottedClassName;
    }
}
