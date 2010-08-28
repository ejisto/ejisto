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

package com.ejisto.event.listener;

import static ch.lambdaj.Lambda.forEach;
import static ch.lambdaj.Lambda.having;
import static ch.lambdaj.Lambda.on;
import static ch.lambdaj.Lambda.select;
import static ch.lambdaj.Lambda.var;
import static com.ejisto.util.GuiUtils.centerOnScreen;
import static com.ejisto.util.GuiUtils.getMessage;
import static com.ejisto.util.GuiUtils.showWarning;
import static com.ejisto.util.IOUtils.deleteFile;
import static com.ejisto.util.IOUtils.findAllWebApplicationClasses;
import static com.ejisto.util.IOUtils.getClasspathEntries;
import static com.ejisto.util.IOUtils.getFilenameWithoutExt;
import static com.ejisto.util.IOUtils.retrieveFilenameFromZipEntryPath;
import static com.ejisto.util.IOUtils.writeFile;
import static org.hamcrest.Matchers.notNullValue;

import java.awt.Dialog.ModalityType;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtField;
import javassist.LoaderClassPath;
import javassist.NotFoundException;

import javax.annotation.Resource;
import javax.swing.Action;
import javax.swing.JFileChooser;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileFilter;

import org.apache.log4j.Logger;
import org.eclipse.jetty.server.HandlerContainer;
import org.eclipse.jetty.webapp.WebAppContext;
import org.jdesktop.swingx.JXDialog;
import org.springframework.context.ApplicationListener;
import org.springframework.util.Assert;

import ch.lambdaj.function.closure.Closure0;
import ch.lambdaj.function.closure.Closure1;

import com.ejisto.constants.StringConstants;
import com.ejisto.core.classloading.EjistoClassLoader;
import com.ejisto.event.EventManager;
import com.ejisto.event.def.ApplicationError;
import com.ejisto.event.def.ApplicationError.Priority;
import com.ejisto.event.def.LoadWebApplication;
import com.ejisto.modules.dao.MockedFieldsDao;
import com.ejisto.modules.dao.entities.MockedField;
import com.ejisto.modules.gui.Application;
import com.ejisto.modules.gui.components.ApplicationInstallerWizard;
import com.ejisto.modules.gui.components.EjistoDialog;
import com.ejisto.modules.gui.components.helper.CallbackAction;
import com.ejisto.modules.gui.components.helper.Step;
import com.ejisto.util.WebApplicationDescriptor;

public class WebApplicationLoader implements ApplicationListener<LoadWebApplication>, ActionListener {

    private static final Logger logger = Logger.getLogger(WebApplicationLoader.class);
    private static final String NEXT_STEP_COMMAND   = "next";
    private static final String PREVIOUS_STEP_COMMAND   = "previous";

    private Pattern contextExtractor = Pattern.compile("^[/a-zA-Z0-9\\s\\W]+(/.+?)/?$");
    @Resource
    private Application application;
    @Resource
    private EventManager eventManager;
    @Resource
    private HandlerContainer contexts;
    @Resource
    private MockedFieldsDao mockedFieldsDao;
    private Step currentStep = Step.FILE_SELECTION;
    private ApplicationInstallerWizard wizard;
    private EjistoDialog dialog;
    private File selectedFile;
    private WebApplicationDescriptor webApplicationDescriptor;
    private Closure1<ActionEvent> callActionPerformed;
    private Closure0 closeDialog;
    private Closure0 confirm;
    private Closure0 selectFile;
    
    private void initClosures() {
        if(callActionPerformed != null) return;
        callActionPerformed = new Closure1<ActionEvent>() {{of(WebApplicationLoader.this).actionPerformed(var(ActionEvent.class));}};
        closeDialog = new Closure0() {{ of(WebApplicationLoader.this).closeDialog();}};
        confirm = new Closure0() {{ of(WebApplicationLoader.this).confirm();}};
        selectFile = new Closure0() {{of(WebApplicationLoader.this).selectFile();}};
    }

    @Override
    public void onApplicationEvent(LoadWebApplication event) {
        initClosures();
        dialog = new EjistoDialog(application, getMessage("wizard.title"), createWizard(), false);
        dialog.registerAction(new CallbackAction(getMessage("buttons.previous.text"), PREVIOUS_STEP_COMMAND, callActionPerformed));
        dialog.registerAction(new CallbackAction(getMessage("buttons.next.text"), NEXT_STEP_COMMAND, callActionPerformed));
        Action act = new CallbackAction(getMessage("wizard.ok.text"), confirm);
        act.setEnabled(isSummaryStep());
        dialog.registerAction(act);
        dialog.registerAction(new CallbackAction(getMessage("wizard.close.text"), EjistoDialog.CLOSE_ACTION_COMMAND, closeDialog));
        dialog.putAction(ApplicationInstallerWizard.SELECT_FILE_COMMAND, new CallbackAction("...", ApplicationInstallerWizard.SELECT_FILE_COMMAND, selectFile)); 
        wizard.initActions();
        dialog.setModalityType(ModalityType.APPLICATION_MODAL);
        dialog.setSize(600, 500);
        centerOnScreen(dialog);
        dialog.setVisible(true);
    }

    

    private ApplicationInstallerWizard createWizard() {
        wizard = new ApplicationInstallerWizard();
        return wizard;
    }
    
    void closeDialog() {
        if(showExitWarning()) dialog.close();
    }
    
    boolean isSummaryStep() {
        return currentStep == Step.SUMMARY;
    }
    
    void confirm() {
    	changeCurrentStep(currentStep);
    }

    void selectFile() {
        selectedFile = openFileSelectionDialog();
        if (selectedFile != null) {
            Thread t = new Thread() {
                @Override
                public void run() {
                    processFile();
                }
            };
            t.start();
        }

    }

    private void processFile() {
        try {
            if (selectedFile == null || currentStep != Step.FILE_SELECTION) return;
            wizard.setSelectedFile(selectedFile.getAbsolutePath());
            this.webApplicationDescriptor=new WebApplicationDescriptor();
            changeCurrentStep(Step.FILE_EXTRACTION);
            String webappBasePath = openWar(selectedFile);
            if (webappBasePath == null)
                return;
            this.webApplicationDescriptor.setInstallationPath(webappBasePath);
            wizard.fileExtractionCompleted(getMessage("progress.file.extraction.end", selectedFile.getName()));
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            eventManager.publishEvent(new ApplicationError(this, ApplicationError.Priority.HIGH, e));
            changeCurrentStep(Step.FILE_SELECTION);
        }
    }

    private void processWebApplication(String basePath) throws Exception {
        Assert.notNull(this.webApplicationDescriptor);
        webApplicationDescriptor.setContextPath(getContextPath(basePath));
        webApplicationDescriptor.setClasspathEntries(getClasspathEntries(basePath));
        loadAllClasses(findAllWebApplicationClasses(basePath, webApplicationDescriptor), webApplicationDescriptor);
        wizard.jobCompleted(getMessage("progress.scan.end"));
    }

    private void loadAllClasses(Collection<String> classnames, WebApplicationDescriptor descriptor) throws NotFoundException {
        notifyStart(classnames.size());
        ClassPool cp = ClassPool.getDefault();
        cp.appendClassPath(new LoaderClassPath(new URLClassLoader(descriptor.getClasspathEntries())));
        CtClass clazz;
        for (String classname : classnames) {
            notifyJobCompleted(classname);
            clazz = cp.get(classname);
            fillMockedFields(cp, clazz, descriptor);
            clazz.detach();
        }
        logger.info("just finished processing "+descriptor.getInstallationPath());
    }

    private void fillMockedFields(ClassPool cp, CtClass clazz, WebApplicationDescriptor descriptor) throws NotFoundException {
        MockedField mockedField;
        try {
            for (CtField field : clazz.getDeclaredFields()) {
                mockedField = new MockedField();
                mockedField.setContextPath(descriptor.getContextPath());
                mockedField.setClassName(clazz.getName());
                mockedField.setFieldName(field.getName());
                mockedField.setFieldType(field.getType().getName());
                descriptor.addField(mockedField);
            }

            CtClass superclass = clazz.getSuperclass();
            if (!superclass.getName().startsWith("java"))
                fillMockedFields(cp, superclass, descriptor);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private File openFileSelectionDialog() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new FileFilter() {
            @Override
            public boolean accept(File f) {
                return f.isDirectory() || f.getName().endsWith(".war");
            }

            @Override
            public String getDescription() {
                return "*.war";
            }
        });
        if (fileChooser.showOpenDialog(application) == JFileChooser.APPROVE_OPTION)
            return fileChooser.getSelectedFile();
        else
            return null;
    }

    private String openWar(File file) {
        try {
        	File baseDir = new File(System.getProperty(StringConstants.JETTY_WEBAPPS_DIR.getValue()) + getFilenameWithoutExt(file) + File.separator);
        	if(!overwriteDir(baseDir)) return null;
            ZipFile war = new ZipFile(file);
            Enumeration<? extends ZipEntry> entries = war.entries();
            ZipEntry entry;
            while (entries.hasMoreElements()) {
                entry = entries.nextElement();
                if (!entry.isDirectory())
                    writeFile(war.getInputStream(entry), baseDir, entry.getName());
                if(entry.getName().endsWith(".jar")) webApplicationDescriptor.addJarFileName(retrieveFilenameFromZipEntryPath(entry.getName()));
            }
            return baseDir.getAbsolutePath();
        } catch (IOException e) {
            eventManager.publishEvent(new ApplicationError(this, ApplicationError.Priority.HIGH, e));
            return null;
        }
    }
    
    private boolean overwriteDir(File dir) {
    	if(dir.exists() && showWarning(wizard, "wizard.overwrite.dir.message", dir.getAbsolutePath())) 
    		return deleteFile(dir);
        return !dir.exists();
    }

    private void deployWebApp(String webAppBasePath) {
        if (webAppBasePath == null)
            return;

        WebAppContext context = new WebAppContext(contexts, webAppBasePath, getContextPath(webAppBasePath));
        context.setResourceBase(webAppBasePath);

        try {
            EjistoClassLoader classLoader = new EjistoClassLoader(webAppBasePath, mockedFieldsDao.loadContextPathFields(context.getContextPath()), context);
            context.setClassLoader(classLoader);
            context.setParentLoaderPriority(false);
            context.start();
        } catch (Exception e) {
            eventManager.publishEvent(new ApplicationError(this, ApplicationError.Priority.HIGH, e));
            logger.error(e.getMessage(), e);
        }
    }

    private boolean showExitWarning() {
        return showWarning(wizard, "wizard.quit.message");
    }
    
    

    protected String getContextPath(String realPath) {
        Matcher matcher = contextExtractor.matcher(realPath.replaceAll(Pattern.quote("\\"), "/"));
        if (matcher.matches())
            return matcher.group(1);
        return null;
    }

    private void changeCurrentStep(Step newStep) {
        if (currentStep == Step.SUMMARY) {
            processSummary();
        } else {
            Assert.notNull(newStep);
            if (Math.abs(currentStep.getIndex() - newStep.getIndex()) != 1)
                throw new IllegalArgumentException(currentStep + " is not compatible with " + newStep);
            switch (newStep) {
            case FILE_EXTRACTION:
                prepareFileExtraction();
                break;
            case CLASSES_FILTERING:
                prepareClassesFiltering();
                break;
            case APPLICATION_SCANNING:
                prepareApplicationScanning();
                break;
            case PROPERTIES_EDITING:
                preparePropertiesEditing();
                break;
            case SUMMARY:
                prepareSummary();
            default:
                break;
            }
            wizard.goToStep(newStep);
            this.currentStep = newStep;
        }
    }

    private void prepareFileExtraction() {
        Assert.notNull(selectedFile);
    }

    private void preparePropertiesEditing() {
        Assert.notNull(this.webApplicationDescriptor);
        Assert.notNull(this.webApplicationDescriptor.getFields());
        wizard.setMockedFields(new ArrayList<MockedField>(this.webApplicationDescriptor.getFields()));
    }

    private void prepareSummary() {
        Assert.notEmpty(this.webApplicationDescriptor.getFields());
        wizard.setSummaryFields(getModifiedFields());
        wizard.getActionMap().get(JXDialog.EXECUTE_ACTION_COMMAND).setEnabled(true);
        dialog.getRootPane().getActionMap().get(NEXT_STEP_COMMAND).setEnabled(false);
    }
    
    private void processSummary() {
    	List<MockedField> modifiedFields = getModifiedFields();
    	forEach(modifiedFields).setContextPath(webApplicationDescriptor.getContextPath());
        mockedFieldsDao.insert(modifiedFields);
        deployWebApp(webApplicationDescriptor.getInstallationPath());
    }

    private List<MockedField> getModifiedFields() {
    	return select(webApplicationDescriptor.getFields(), having(on(MockedField.class).getFieldValue(), notNullValue()));
    }

    private void prepareApplicationScanning() {
        Assert.notNull(selectedFile);
        this.webApplicationDescriptor.setBlacklist(wizard.getBlacklistedJars());
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    processWebApplication(webApplicationDescriptor.getInstallationPath());
                } catch (Exception e) {
                    eventManager.publishEvent(new ApplicationError(this, Priority.HIGH, e));
                }
            }
        });
    }
    
    private void prepareClassesFiltering() {
        Assert.notEmpty(this.webApplicationDescriptor.getIncludedJars());
        wizard.setJars(webApplicationDescriptor.getIncludedJars());
    }
    
    private void notifyStart(int numJobs) {
        wizard.startProgress(getMessage("progress.scan.start"), numJobs);
    }

    private void notifyJobCompleted(String nextClass) {
        wizard.jobCompleted(getMessage("progress.scan.class", nextClass));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        final Step newStep;
        if (e.getActionCommand().equals(NEXT_STEP_COMMAND)) {
            newStep = Step.nextStep(currentStep);
        } else {
            newStep = Step.previousStep(currentStep);
        }
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                changeCurrentStep(newStep);
            }
        });
    }

}
