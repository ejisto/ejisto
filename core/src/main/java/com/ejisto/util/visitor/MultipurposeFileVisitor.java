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

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Created by IntelliJ IDEA.
 * User: celestino
 * Date: 2/16/13
 * Time: 9:31 PM
 */
public class MultipurposeFileVisitor<T> extends SimpleFileVisitor<T> {

    private final FileVisitor<T> master;
    private final List<FileVisitor<T>> additionalVisitors;

    @SafeVarargs
    public MultipurposeFileVisitor(FileVisitor<T> master, FileVisitor<T>... additional) {
        Objects.requireNonNull(master);
        this.master = master;
        this.additionalVisitors = Collections.unmodifiableList(Arrays.asList(additional));
    }

    @Override
    public FileVisitResult preVisitDirectory(T dir, BasicFileAttributes attrs) throws IOException {
        for (FileVisitor<T> visitor : additionalVisitors) {
            validateAdditionalVisitorResult(visitor.preVisitDirectory(dir, attrs));
        }
        return master.preVisitDirectory(dir, attrs);
    }

    @Override
    public FileVisitResult visitFile(T file, BasicFileAttributes attrs) throws IOException {
        for (FileVisitor<T> visitor : additionalVisitors) {
            validateAdditionalVisitorResult(visitor.visitFile(file, attrs));
        }
        return master.visitFile(file, attrs);
    }

    @Override
    public FileVisitResult visitFileFailed(T file, IOException exc) throws IOException {
        for (FileVisitor<T> visitor : additionalVisitors) {
            validateAdditionalVisitorResult(visitor.visitFileFailed(file, exc));
        }
        return master.visitFileFailed(file, exc);
    }

    @Override
    public FileVisitResult postVisitDirectory(T dir, IOException exc) throws IOException {
        for (FileVisitor<T> visitor : additionalVisitors) {
            validateAdditionalVisitorResult(visitor.postVisitDirectory(dir, exc));
        }
        return master.postVisitDirectory(dir, exc);
    }

    private static void validateAdditionalVisitorResult(FileVisitResult result) {
        if (!Objects.equals(FileVisitResult.CONTINUE, result)) {
            throw new IllegalStateException("An optional visitor cannot return a result other than CONTINUE");
        }
    }
}
