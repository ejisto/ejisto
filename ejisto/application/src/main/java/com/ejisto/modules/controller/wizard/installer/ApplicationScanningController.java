package com.ejisto.modules.controller.wizard.installer;

import static com.ejisto.util.GuiUtils.getMessage;
import static com.ejisto.util.IOUtils.findAllWebApplicationClasses;
import static com.ejisto.util.IOUtils.getClasspathEntries;

import java.net.URLClassLoader;
import java.util.Collection;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtField;
import javassist.LoaderClassPath;
import javassist.NotFoundException;

import org.apache.log4j.Logger;
import org.springframework.util.Assert;

import com.ejisto.modules.controller.WizardException;
import com.ejisto.modules.dao.entities.MockedField;
import com.ejisto.modules.gui.components.EjistoDialog;
import com.ejisto.modules.gui.components.ProgressPanel;
import com.ejisto.modules.gui.components.helper.Step;
import com.ejisto.util.WebApplicationDescriptor;

public class ApplicationScanningController extends AbstractApplicationInstallerController implements Callable<Void> {
    private static final Logger logger = Logger.getLogger(ApplicationScanningController.class);
    private Pattern contextExtractor = Pattern.compile("^[/a-zA-Z0-9\\s\\W]+(/.+?)/?$");
    private FutureTask<Void> task;
    private ProgressPanel applicationScanningTab;

    public ApplicationScanningController(EjistoDialog dialog) {
        super(dialog);
    }

    @Override
    public ProgressPanel getView() {
        if(applicationScanningTab != null) return applicationScanningTab;
        applicationScanningTab=new ProgressPanel();
        return applicationScanningTab;
    }

    @Override
    public boolean canProceed() {
        return true;
    }

    @Override
    public boolean isExecutionSucceeded() throws WizardException {
        return task.isDone();
    }

    @Override
    public Step getStep() {
        return Step.APPLICATION_SCANNING;
    }

    @Override
    public void activate() {
        task = new FutureTask<Void>(this);
    }

    @Override
    public boolean executionCompleted() {
        try {
            task.get();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public Void call() throws Exception {
        processWebApplication();
        return null;
    }
    
    private void processWebApplication() throws Exception {
        Assert.notNull(getSession().getInstallationPath());
        String basePath = getSession().getInstallationPath();
        getSession().setContextPath(getContextPath(basePath));
        getSession().setClasspathEntries(getClasspathEntries(basePath));
        loadAllClasses(findAllWebApplicationClasses(basePath, getSession()), getSession());
        getView().jobCompleted(getMessage("progress.scan.end"));
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

            CtClass zuperclazz = clazz.getSuperclass();
            if (!zuperclazz.getName().startsWith("java"))
                fillMockedFields(cp, zuperclazz, descriptor);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private String getContextPath(String realPath) {
        Matcher matcher = contextExtractor.matcher(realPath.replaceAll(Pattern.quote("\\"), "/"));
        if (matcher.matches())
            return matcher.group(1);
        return null;
    }
    
    private void notifyStart(int numJobs) {
        getView().initProgress(numJobs, getMessage("progress.scan.start"));
    }
    
    private void notifyJobCompleted(String nextClass) {
        getView().jobCompleted(getMessage("progress.scan.class", nextClass));
    }

}
