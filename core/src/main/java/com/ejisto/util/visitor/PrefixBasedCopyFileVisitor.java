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

package com.ejisto.util.visitor;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;

import static org.apache.commons.lang3.StringUtils.defaultString;

/**
 * Created by IntelliJ IDEA.
 * User: celestino
 * Date: 11/9/12
 * Time: 8:12 AM
 */
public class PrefixBasedCopyFileVisitor extends SimpleFileVisitor<Path> {
    private final CopyOptions options;

    public PrefixBasedCopyFileVisitor(CopyOptions options) {
        this.options = options;
    }

    @Override
    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
        Path dest = options.targetRoot.resolve(options.srcRoot.relativize(dir));
        if (!Files.exists(dest)) {
            Files.createDirectories(dest);
        }
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        String fileName = file.getFileName().toString();
        boolean matches = options.prefixesList.isEmpty()
                || options.prefixesList.stream()
                    .filter(fileName::startsWith)
                    .findFirst().isPresent();
        copy(file, matches);
        return FileVisitResult.CONTINUE;
    }

    private void copy(Path srcFile, boolean nameMatches) throws IOException {
        if (!isCopyAllowed(nameMatches, options)) {
            return;
        }
        Path relative = options.srcRoot.relativize(srcFile);
        int count = relative.getNameCount();
        StringBuilder newFileName = new StringBuilder();
        if (count > 1) {
            newFileName.append(relative.getParent().toString());
            newFileName.append(File.separator);
        }
        newFileName.append(defaultString(options.filesPrefix)).append(srcFile.getFileName().toString());
        Files.copy(srcFile, options.targetRoot.resolve(newFileName.toString()), StandardCopyOption.REPLACE_EXISTING);
    }

    private boolean isCopyAllowed(boolean nameMatches, CopyOptions options) {
        switch (options.copyType) {
            case INCLUDE_ALL:
                return true;
            case INCLUDE_ONLY_MATCHING_RESOURCES:
                return nameMatches;
            case EXCLUDE_MATCHING_RESOURCES:
                return !nameMatches;
            default:
                return true;
        }
    }

    public static class CopyOptions {

        private final Path srcRoot;
        private final Path targetRoot;
        private final List<String> prefixesList;
        private final String filesPrefix;
        private CopyType copyType;


        public CopyOptions(Path srcRoot, Path targetRoot, List<String> prefixesList, String filesPrefix, CopyType copyType) {
            this.srcRoot = srcRoot;
            this.targetRoot = targetRoot;
            this.prefixesList = prefixesList;
            this.filesPrefix = filesPrefix;
            this.copyType = copyType;
        }
    }

    public enum CopyType {
        INCLUDE_ALL,
        INCLUDE_ONLY_MATCHING_RESOURCES,
        EXCLUDE_MATCHING_RESOURCES
    }


}
