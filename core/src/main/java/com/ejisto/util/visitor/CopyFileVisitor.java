/*
 * Ejisto, a powerful developer assistant
 *
 * Copyright (C) 2010-2012  Celestino Bellone
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

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

/**
 * Created by IntelliJ IDEA.
 * User: celestino
 * Date: 11/9/12
 * Time: 8:16 AM
 */
public class CopyFileVisitor extends SimpleFileVisitor<Path> {
    private final Path srcBaseDir;
    private final Path targetBaseDir;

    public CopyFileVisitor(Path srcBaseDir, Path targetBaseDir) {
        this.srcBaseDir = srcBaseDir;
        this.targetBaseDir = targetBaseDir;
    }

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        Files.copy(file, targetBaseDir.resolve(srcBaseDir.relativize(file).toString()), REPLACE_EXISTING);
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
        Path destinationDir = targetBaseDir.resolve(srcBaseDir.relativize(dir).toString());
        if (Files.notExists(destinationDir)) {
            Files.createDirectories(destinationDir);
        }
        return FileVisitResult.CONTINUE;
    }
}
