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

package com.ejisto.core.classloading;

import org.apache.log4j.Logger;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class SharedClassLoader extends URLClassLoader {

    private static final Logger logger = Logger.getLogger(SharedClassLoader.class);

    private Set<String> entries;

    public SharedClassLoader() {
        super(new URL[0]);
        entries = Collections.synchronizedSet(new HashSet<String>());
    }

    public void addEntry(String entry) {
        if (!entries.contains(entry)) {
            try {
                super.addURL(new File(entry).toURI().toURL());
                entries.add(entry);
            } catch (MalformedURLException e) {
                logger.error("unable to add resource " + entry, e);
            }
        }
    }

    public void addEntries(Collection<String> entries) {
        for (String entry : entries) {
            addEntry(entry);
        }
    }

    @Override
    public Class<?> loadClass(String name) {
        try {
            return super.loadClass(name);
        } catch (ClassNotFoundException ex) {
            return null;
        }
    }
}
