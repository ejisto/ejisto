/*******************************************************************************
 * Copyright 2010 Celestino Bellone
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package com.ejisto.modules.controller.wizard.installer;

import static com.ejisto.util.GuiUtils.getMessage;
import static com.ejisto.util.GuiUtils.showWarning;
import static com.ejisto.util.IOUtils.deleteFile;
import static com.ejisto.util.IOUtils.getFilenameWithoutExt;
import static com.ejisto.util.IOUtils.retrieveFilenameFromZipEntryPath;
import static com.ejisto.util.IOUtils.writeFile;

import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.concurrent.Future;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import com.ejisto.constants.StringConstants;
import com.ejisto.modules.controller.WizardException;
import com.ejisto.modules.gui.components.EjistoDialog;
import com.ejisto.modules.gui.components.ProgressPanel;
import com.ejisto.modules.gui.components.helper.Step;

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
        File baseDir = new File(System.getProperty(StringConstants.JETTY_WEBAPPS_DIR.getValue()) + getFilenameWithoutExt(file) + File.separator);
        if (!overwriteDir(baseDir)) return null;
        ZipFile war = new ZipFile(file);
        Enumeration<? extends ZipEntry> entries = war.entries();
        getView().initProgress(war.size(),getMessage("progress.start")); 
        ZipEntry entry;
        while (entries.hasMoreElements()) {
            entry = entries.nextElement();
            getView().jobCompleted(getMessage("progress.file.extraction", entry.getName()));
            if (!entry.isDirectory()) writeFile(war.getInputStream(entry), baseDir, entry.getName());
            if (entry.getName().endsWith(".jar")) getSession().addJarFileName(retrieveFilenameFromZipEntryPath(entry.getName()));
        }
        return baseDir.getAbsolutePath();
    }

    private boolean overwriteDir(File dir) {
        if (dir.exists() && showWarning(getDialog(), "wizard.overwrite.dir.message", dir.getAbsolutePath())) return deleteFile(dir);
        return !dir.exists();
    }

	@Override
	public String getTitleKey() {
		return "wizard.fileextraction.title";
	}

	@Override
	public String getDescriptionKey() {
		return "wizard.fileextraction.description";
	}
    
}
