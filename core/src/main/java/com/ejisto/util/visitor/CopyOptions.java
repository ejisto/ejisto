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

package com.ejisto.util.visitor;

import java.nio.file.Path;

/**
* Created by IntelliJ IDEA.
* User: celestino
* Date: 2/14/14
* Time: 8:02 AM
*/
public class CopyOptions {

    final Path srcRoot;
    final Path targetRoot;
    final FileMatcher matcher;
    final String filesPrefix;
    final CopyType copyType;


    public CopyOptions(Path srcRoot, Path targetRoot, FileMatcher matcher, String filesPrefix, CopyType copyType) {
        this.srcRoot = srcRoot;
        this.targetRoot = targetRoot;
        this.matcher = matcher;
        this.filesPrefix = filesPrefix;
        this.copyType = copyType;
    }
}
