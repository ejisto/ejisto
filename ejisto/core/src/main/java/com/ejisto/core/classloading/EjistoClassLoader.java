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

package com.ejisto.core.classloading;

import com.ejisto.constants.StringConstants;
import com.ejisto.modules.repository.MockedFieldsRepository;
import org.apache.log4j.Logger;
import org.eclipse.jetty.webapp.WebAppClassLoader;
import org.eclipse.jetty.webapp.WebAppContext;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Collection;

public class EjistoClassLoader extends WebAppClassLoader {

    private static final Logger logger = Logger.getLogger(EjistoClassLoader.class);
    private ClassTransformer transformer;
    private String contextPath;
    private String installationPath;
    private MockedFieldsRepository mockedFieldsRepository;

    public EjistoClassLoader(String installationPath, WebAppContext context) throws IOException {
        super(SharedClassLoader.getInstance(), context);
//        InstrumentationHolder.getInstrumentation().addTransformer(new ClassTransformer(this, mockedFields), true);
        this.installationPath = installationPath;
        this.contextPath = context.getContextPath();
        this.transformer = new ClassTransformer(context.getContextPath());
        this.mockedFieldsRepository = MockedFieldsRepository.getInstance();
        addLibExt();
    }

    @Override
    public Class<?> findClass(String name) throws ClassNotFoundException {
        try {
            boolean instrumentableClass = isInstrumentableClass(name);
            if (logger.isDebugEnabled()) logger.debug(name + " is mockable class: " + instrumentableClass);
            if (instrumentableClass) return loadInstrumentableClass(name);
            return null;//should never happen...
        } catch (Exception e) {
            throw new ClassNotFoundException("Unable to load class [" + name + "]", e);
        }
    }

    public Class<?> loadInstrumentableClass(String name) throws Exception {
        if (logger.isDebugEnabled()) logger.debug("loading instrumentable class: " + name);
        return transformer.transform(name);
    }

    public boolean isInstrumentableClass(String name) {
        return mockedFieldsRepository.isMockableClass(contextPath, name.replaceAll("/", "."));
    }

    public String getContextPath() {
        return contextPath;
    }

    public String getInstallationPath() {
        return installationPath;
    }

    private void addLibExt() {
        try {
            File directory = new File(System.getProperty(StringConstants.EXTENSIONS_DIR.getValue()));
            if (!directory.exists() || !directory.isDirectory()) return;
            for (File f : directory.listFiles()) {
                if (f.getName().endsWith(".jar")) addURL(f.toURI().toURL());
            }
        } catch (Exception e) {
            logger.error("unable to load extra classpath", e);
        }
    }

    public void addExtraEntries(Collection<String> entries) throws MalformedURLException {
        for (String entry : entries) {
            addURL(new File(entry).toURI().toURL());
        }
    }

}
