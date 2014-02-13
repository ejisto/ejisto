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

import com.ejisto.core.classloading.ClassTransformer;
import com.ejisto.modules.repository.MockedFieldsRepository;
import org.apache.log4j.Logger;
import org.springsource.loaded.LoadtimeInstrumentationPlugin;
import org.springsource.loaded.ReloadEventProcessorPlugin;

import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;
import java.util.Optional;

import static com.ejisto.constants.StringConstants.EJISTO_CLASS_TRANSFORMER_CATEGORY;

/**
 * Created by IntelliJ IDEA.
 * User: celestino
 * Date: 2/11/14
 * Time: 8:28 AM
 */
public class SpringLoadedJVMPlugin implements ReloadEventProcessorPlugin, LoadtimeInstrumentationPlugin {
    private static final Logger logger = Logger.getLogger(EJISTO_CLASS_TRANSFORMER_CATEGORY.getValue());
    private final ClassTransformer transformer;

    public SpringLoadedJVMPlugin(ClassTransformer transformer) {
        this.transformer = transformer;
    }

    @Override
    public boolean accept(String slashedTypeName, ClassLoader classLoader, ProtectionDomain protectionDomain, byte[] bytes) {
        return slashedTypeName != null && transformer.isInstrumentableClass(toDottedClassName(slashedTypeName));
    }

    @Override
    public byte[] modify(String slashedClassName, ClassLoader classLoader, byte[] bytes) {
        try {
            return transformer.transform(classLoader, toDottedClassName(slashedClassName), null, null, bytes);
        } catch (IllegalClassFormatException e) {
            logger.warn(String.format("unable to transform class %s", slashedClassName), e);
            return null;
        }
    }

    @Override
    public boolean shouldRerunStaticInitializer(String typename, Class<?> clazz, String encodedTimestamp) {
        return false;
    }

    @Override
    public void reloadEvent(String typename, Class<?> clazz, String encodedTimestamp) {
        if(logger.isInfoEnabled()) {
            logger.info(String.format("%s has been reloaded", typename));
        }
    }

    private static String toDottedClassName(String slashedClassName) {
        return slashedClassName.replaceAll("/", ".");
    }
}
