/*
 * Copyright 2010 Celestino Bellone
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions
 * and limitations under the License.
 */
package com.ejisto.util;

import java.io.File;
import java.io.FileFilter;
import java.util.regex.Pattern;

public class FileExtensionFilter implements FileFilter {

    public static final Pattern ALL_JARS    = Pattern.compile("^.*?\\.jar$");
    public static final Pattern ALL_CLASSES = Pattern.compile("^.*?\\.class$");
    private Pattern pattern;
    private boolean includeDirs;

    public FileExtensionFilter(Pattern pattern, boolean includeDirs) {
        this.pattern=pattern;
        this.includeDirs=includeDirs;
    }
    
    public FileExtensionFilter() {
    	this.includeDirs=true;
    }

    @Override
    public boolean accept(File pathname) {
        return (includeDirs && pathname.isDirectory()) || pattern == null || pattern.matcher(pathname.getName()).matches();
    }
}
