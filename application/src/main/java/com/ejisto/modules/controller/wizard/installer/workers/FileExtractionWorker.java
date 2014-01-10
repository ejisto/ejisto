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

package com.ejisto.modules.controller.wizard.installer.workers;

import com.ejisto.modules.controller.wizard.installer.FileExtractionController;
import com.ejisto.modules.dao.entities.WebApplicationDescriptor;
import com.ejisto.modules.dao.entities.WebApplicationDescriptorElement;
import com.ejisto.modules.executor.GuiTask;
import com.ejisto.util.IOUtils;
import lombok.extern.log4j.Log4j;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

import static com.ejisto.modules.executor.ProgressDescriptor.ProgressState.COMPLETED;
import static com.ejisto.modules.executor.ProgressDescriptor.ProgressState.RUNNING;
import static com.ejisto.util.GuiUtils.getMessage;
import static com.ejisto.util.GuiUtils.showWarning;
import static com.ejisto.util.IOUtils.getFilenameWithoutExt;

/**
 * Created by IntelliJ IDEA.
 * User: celestino
 * Date: 11/29/11
 * Time: 7:09 PM
 */
@Log4j
public class FileExtractionWorker extends GuiTask<Void> {

    private WebApplicationDescriptor session;

    public FileExtractionWorker(WebApplicationDescriptor session, FileExtractionController controller) {
        this.session = session;
        addPropertyChangeListener(controller);
    }

    @Override
    protected Void internalDoInBackground() throws IOException, InvocationTargetException, InterruptedException {
        File war = getSession().getWarFile();

        String path = openWar(war);
        getSession().setInstallationPath(path);
        notifyJobCompleted(COMPLETED, getMessage("progress.file.extraction.end", war.getName()));
        return null;
    }

    private String openWar(File file) throws IOException {
        String newPath = System.getProperty("java.io.tmpdir") + File.separator + getFilenameWithoutExt(
                file) + File.separator;
        File baseDir = new File(newPath);
        if (!overwriteDir(baseDir)) {
            return null;
        }
        initTempDir(baseDir);
        getSession().clearElements();
        IOUtils.unzipFile(file, newPath, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                try {
                    String fileName = file.getFileName().toString();
                    notifyJobCompleted(RUNNING, getMessage("progress.file.extraction", fileName));
                    if (fileName.endsWith(".jar")) {
                        getSession().addElement(new WebApplicationDescriptorElement(fileName));
                    }
                } catch (InterruptedException | InvocationTargetException e) {
                    FileExtractionWorker.log.warn("unexpected exception", e);
                }
                return FileVisitResult.CONTINUE;
            }
        });
        return newPath;
    }

    private boolean overwriteDir(File dir) {
        if (dir.exists() && showWarning(null, "wizard.overwrite.dir.message", dir.getAbsolutePath())) {
            return IOUtils.emptyDir(dir);
        }
        return !dir.exists();
    }

    private void initTempDir(File dir) throws IOException {
        Files.createDirectory(dir.toPath());
    }

    private WebApplicationDescriptor getSession() {
        return session;
    }


}
