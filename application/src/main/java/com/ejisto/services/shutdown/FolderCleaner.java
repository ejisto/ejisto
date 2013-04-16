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

package com.ejisto.services.shutdown;

import lombok.extern.log4j.Log4j;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;

/**
 * Created by IntelliJ IDEA.
 * User: celestino
 * Date: 8/22/12
 * Time: 6:41 PM
 */
@Log4j
public class FolderCleaner extends BaseShutdownService {

    @Override
    public void execute() {
        Path path = FileSystems.getDefault().getPath(System.getProperty("java.io.tmpdir"));
        try {
            deleteAllFiles(path);
        } catch (IOException e) {
            log.warn("unable to delete temporary files", e);
        }
    }

    @Override
    public int getPriority() {
        return Integer.MAX_VALUE;
    }

    private void deleteAllFiles(Path path) throws IOException {
        Files.walkFileTree(path, new FileVisitor<Path>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                if (attrs.isRegularFile()) {
                    Files.delete(file);
                }
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
                log.warn("unable to delete file " + file.toString(), exc);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                if (exc == null) {
                    Files.delete(dir);
                }
                return FileVisitResult.CONTINUE;
            }
        });
    }
}
