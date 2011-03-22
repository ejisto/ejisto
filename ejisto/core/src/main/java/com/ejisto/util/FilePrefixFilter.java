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

/**
 * Created by IntelliJ IDEA.
 * User: celestino
 * Date: 3/22/11
 * Time: 9:58 PM
 */
public class FilePrefixFilter implements FileFilter {

    private String[] prefixes;

    public FilePrefixFilter(String[] prefixes) {
        this.prefixes = prefixes;
    }

    @Override
    public boolean accept(File pathName) {
        for (String prefix : prefixes) {
            if (pathName.getName().startsWith(prefix)) return true;
        }
        return false;
    }
}
