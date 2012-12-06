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

package com.ejisto.modules.controller.wizard.installer.workers;

import com.ejisto.modules.controller.wizard.installer.FileExtractionController;
import com.ejisto.modules.dao.entities.WebApplicationDescriptor;
import com.ejisto.modules.dao.entities.WebApplicationDescriptorElement;
import com.ejisto.modules.executor.GuiTask;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import static com.ejisto.modules.executor.ProgressDescriptor.ProgressState.COMPLETED;
import static com.ejisto.modules.executor.ProgressDescriptor.ProgressState.RUNNING;
import static com.ejisto.util.GuiUtils.getMessage;
import static com.ejisto.util.GuiUtils.showWarning;
import static com.ejisto.util.IOUtils.*;

/**
 * Created by IntelliJ IDEA.
 * User: celestino
 * Date: 11/29/11
 * Time: 7:09 PM
 */
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

    private String openWar(File file) throws IOException, InvocationTargetException, InterruptedException {
        String newPath = System.getProperty("java.io.tmpdir") + File.separator + getFilenameWithoutExt(
                file) + File.separator;
        File baseDir = new File(newPath);
        if (!overwriteDir(baseDir)) {
            return null;
        }
        if (!initTempDir(baseDir)) {
            throw new IOException("Path " + baseDir.getAbsolutePath() + " is not writable. Cannot continue.");
        }
        getSession().clearElements();

        //IOUtils.unzipFile(file, newPath);

        ZipFile war = new ZipFile(file);
        Enumeration<? extends ZipEntry> entries = war.entries();
        firePropertyChange("startProgress", 0, war.size());
        ZipEntry entry;
        while (entries.hasMoreElements()) {
            entry = entries.nextElement();
            notifyJobCompleted(RUNNING, getMessage("progress.file.extraction", entry.getName()));
            if (!entry.isDirectory()) {
                writeFile(war.getInputStream(entry), baseDir, entry.getName());
            }
            if (entry.getName().endsWith(".jar")) {
                getSession().addElement(
                        new WebApplicationDescriptorElement(retrieveFilenameFromPath(entry.getName())));
            }
        }
        return newPath;
    }

    private boolean overwriteDir(File dir) {
        if (dir.exists() && showWarning(null, "wizard.overwrite.dir.message", dir.getAbsolutePath())) {
            boolean success = true;
            for (File f : dir.listFiles()) {
                success &= deleteFile(f);
            }
            return success;
        }
        return !dir.exists();
    }

    private boolean initTempDir(File dir) {
        if (dir.exists()) {
            return true;
        }
        boolean success = dir.mkdir();
        if (!success) {
            return success;
        }
        dir.deleteOnExit();
        return success;
    }

    private WebApplicationDescriptor getSession() {
        return session;
    }


}
