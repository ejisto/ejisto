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

package com.ejisto.modules.controller.wizard.installer;

import com.ejisto.modules.controller.WizardException;
import com.ejisto.modules.dao.entities.WebApplicationDescriptorElement;
import com.ejisto.modules.gui.components.EjistoDialog;
import com.ejisto.modules.gui.components.ProgressPanel;
import com.ejisto.modules.gui.components.helper.Step;

import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.concurrent.Future;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import static com.ejisto.util.GuiUtils.getMessage;
import static com.ejisto.util.GuiUtils.showWarning;
import static com.ejisto.util.IOUtils.*;

public class FileExtractionController extends AbstractApplicationInstallerController implements Runnable {

    private Future<?> task;
    private ProgressPanel fileExtractionTab;

    public FileExtractionController(EjistoDialog dialog) {
        super(dialog);
    }

    @Override
    public ProgressPanel getView() {
        if (fileExtractionTab != null) return fileExtractionTab;
        fileExtractionTab = new ProgressPanel();
        return fileExtractionTab;
    }

    @Override
    public boolean canProceed() {
        return getSession().getWarFile() != null;
    }

    @Override
    public boolean automaticallyProceedToNextStep() {
        return true;
    }

    @Override
    public boolean executionCompleted() {
        return task.isDone();
    }

    @Override
    public boolean isExecutionSucceeded() throws WizardException {
        try {
            task.get();
            return true;
        } catch (Exception e) {
            throw new WizardException(e);
        }
    }

    @Override
    public Step getStep() {
        return Step.FILE_EXTRACTION;
    }

    @Override
    public void activate() {
        this.task = addJob(this);
    }

    @Override
    public void run() {
        try {
            File war = getSession().getWarFile();
            String path = openWar(war);
            getSession().setInstallationPath(path);
            getView().jobCompleted(getMessage("progress.file.extraction.end", war.getName()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String openWar(File file) throws IOException {
        File baseDir = new File(System.getProperty("java.io.tmpdir") + File.separator + getFilenameWithoutExt(file));
        if (!overwriteDir(baseDir)) return null;
        if (!initTempDir(baseDir)) throw new IOException("Path " + baseDir.getAbsolutePath() + " is not writable. Cannot continue.");
        getSession().clearElements();
        ZipFile war = new ZipFile(file);
        Enumeration<? extends ZipEntry> entries = war.entries();
        getView().initProgress(war.size(), getMessage("progress.start"));
        ZipEntry entry;
        while (entries.hasMoreElements()) {
            entry = entries.nextElement();
            getView().jobCompleted(getMessage("progress.file.extraction", entry.getName()));
            if (!entry.isDirectory()) writeFile(war.getInputStream(entry), baseDir, entry.getName());
            if (entry.getName().endsWith(".jar"))
                getSession().addElement(new WebApplicationDescriptorElement(retrieveFilenameFromZipEntryPath(entry.getName())));
        }
        return baseDir.getAbsolutePath();
    }

    private boolean overwriteDir(File dir) {
        if (dir.exists() && showWarning(getDialog(), "wizard.overwrite.dir.message", dir.getAbsolutePath())) {
            boolean success = true;
            for (File f : dir.listFiles()) success &= deleteFile(f);
            return success;
        }
        return !dir.exists();
    }

    private boolean initTempDir(File dir) throws IOException {
        if (dir.exists()) return true;
        boolean success = dir.mkdir();
        if (!success) return success;
        dir.deleteOnExit();
        return success;
    }

    @Override
    public String getTitleKey() {
        return "wizard.fileextraction.title";
    }

    @Override
    public String getDescriptionKey() {
        return "wizard.fileextraction.description";
    }

    @Override
    public boolean isBackEnabled() {
        return false;
    }

}
