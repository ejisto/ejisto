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

import java.io.File;
import java.io.FileFilter;
import java.util.regex.Pattern;

public class FileExtensionFilter implements FileFilter {

    public static final Pattern ALL_JARS = Pattern.compile("^.*?\\.jar$");
    public static final Pattern ALL_CLASSES = Pattern.compile("^.*?\\.class$");
    private Pattern pattern;
    private boolean includeDirs;

    public FileExtensionFilter(Pattern pattern, boolean includeDirs) {
        this.pattern = pattern;
        this.includeDirs = includeDirs;
    }

    public FileExtensionFilter() {
        this.includeDirs = true;
    }

    @Override
    public boolean accept(File pathname) {
        return (includeDirs && pathname.isDirectory()) || pattern == null || pattern.matcher(
                pathname.getName()).matches();
    }
}
