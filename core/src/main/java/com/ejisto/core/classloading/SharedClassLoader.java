/*
 * Ejisto, a powerful developer assistant
 *
 * Copyright (C) 2010-2013 Celestino Bellone
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

import lombok.extern.log4j.Log4j;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collection;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

@Log4j
public final class SharedClassLoader extends URLClassLoader {

    private static final SharedClassLoader INSTANCE = new SharedClassLoader();

    private Set<String> entries;

    public static SharedClassLoader getInstance() {
        return INSTANCE;
    }

    private SharedClassLoader() {
        super(new URL[0]);
        entries = new CopyOnWriteArraySet<>();
    }

    void addEntry(String entry) {
        if (!entries.contains(entry)) {
            try {
                super.addURL(new File(entry).toURI().toURL());
                entries.add(entry);
            } catch (MalformedURLException e) {
                log.error("unable to add resource " + entry, e);
            }
        }
    }

    public void addEntries(Collection<String> entries) {
        entries.forEach(this::addEntry);
    }

}
