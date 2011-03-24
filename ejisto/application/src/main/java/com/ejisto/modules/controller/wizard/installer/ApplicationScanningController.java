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

import com.ejisto.core.classloading.decorator.MockedFieldDecorator;
import com.ejisto.modules.controller.WizardException;
import com.ejisto.modules.dao.entities.CustomObjectFactory;
import com.ejisto.modules.dao.entities.MockedField;
import com.ejisto.modules.dao.entities.WebApplicationDescriptor;
import com.ejisto.modules.gui.components.EjistoDialog;
import com.ejisto.modules.gui.components.ProgressPanel;
import com.ejisto.modules.gui.components.helper.Step;
import com.ejisto.modules.repository.CustomObjectFactoryRepository;
import javassist.*;
import org.apache.log4j.Logger;
import org.codehaus.cargo.module.webapp.WebXml;
import org.codehaus.cargo.module.webapp.WebXmlIo;
import org.codehaus.cargo.module.webapp.WebXmlType;
import org.codehaus.cargo.module.webapp.WebXmlUtils;
import org.jdom.Element;
import org.springframework.util.Assert;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static ch.lambdaj.Lambda.join;
import static com.ejisto.constants.StringConstants.*;
import static com.ejisto.modules.repository.ClassPoolRepository.registerClassPool;
import static com.ejisto.util.GuiUtils.getMessage;
import static com.ejisto.util.IOUtils.*;

public class ApplicationScanningController extends AbstractApplicationInstallerController implements Callable<Void> {
    private static final Logger logger = Logger.getLogger(ApplicationScanningController.class);
    private static String[] entries = {"derbyclient", "derbynet", "ejisto-core", "hamcrest", "javassist", "lambdaj", "objenesis", "ognl", "spring", "cglib", "commons"};
    private Pattern contextExtractor = Pattern.compile("^[/a-zA-Z0-9\\s\\W]+(/.+?)/?$");
    private Future<Void> task;
    private ProgressPanel applicationScanningTab;
    private String containerHome;

    public ApplicationScanningController(EjistoDialog dialog, String containerHome) {
        super(dialog);
        this.containerHome = containerHome;
    }

    @Override
    public ProgressPanel getView() {
        if (applicationScanningTab != null) return applicationScanningTab;
        applicationScanningTab = new ProgressPanel();
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
        task = addJob(this);
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
        getSession().setClassPathElements(getClasspathEntries(basePath));
        loadAllClasses(findAllWebApplicationClasses(basePath, getSession()), getSession());
        processWebXmlDescriptor(getSession());
        packageWar(getSession());
        getView().jobCompleted(getMessage("progress.scan.end"));
    }

    private void loadAllClasses(Collection<String> classnames, WebApplicationDescriptor descriptor) throws NotFoundException, MalformedURLException {
        notifyStart(classnames.size());
        descriptor.deleteAllFields();
        URL[] descriptorEntries = toUrlArray(descriptor);
        ClassLoader loader = new URLClassLoader(addServerLibs(descriptorEntries, containerHome + File.separator + "lib"));
        ClassPool cp = new ClassPool(ClassPool.getDefault());
        cp.appendClassPath(new LoaderClassPath(loader));
        CtClass clazz;
        for (String classname : classnames) {
            notifyJobCompleted(classname);
            clazz = cp.get(classname);
            fillMockedFields(clazz, descriptor, loader);
            clazz.detach();
        }
        registerClassPool(descriptor.getContextPath(), cp);
        logger.info("just finished processing " + descriptor.getInstallationPath());
    }

    private void fillMockedFields(CtClass clazz, WebApplicationDescriptor descriptor, ClassLoader loader) throws NotFoundException {
        MockedField mockedField;
        try {
            for (CtField field : clazz.getDeclaredFields()) {
                mockedField = new MockedFieldDecorator();
                mockedField.setContextPath(descriptor.getContextPath());
                mockedField.setClassName(clazz.getName());
                mockedField.setFieldName(field.getName());
                mockedField.setFieldType(field.getType().getName());
                parseGenerics(clazz, field, mockedField, loader);
                descriptor.addField(mockedField);
            }
            CtClass zuperclazz = clazz.getSuperclass();
            if (!zuperclazz.getName().startsWith("java")) fillMockedFields(zuperclazz, descriptor, loader);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void parseGenerics(CtClass clazz, CtField field, MockedField mockedField, ClassLoader loader) {
        try {
            Class<?> cl = loader.loadClass(clazz.getName());
            Field f = cl.getDeclaredField(field.getName());
            Type type = f.getGenericType();
            if (ParameterizedType.class.isAssignableFrom(type.getClass())) {
                List<String> generics = new ArrayList<String>();
                deepParseGenerics((ParameterizedType) type, generics);
                mockedField.setFieldElementType(join(generics));
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    private void deepParseGenerics(ParameterizedType type, List<String> target) {
        Type[] types = type.getActualTypeArguments();
        for (Type generic : types) {
            if (ParameterizedType.class.isAssignableFrom(generic.getClass())) {
                //TODO implement deep inspection
                target.add(((ParameterizedType) generic).getRawType().getClass().getName());
            } else {
                target.add(((Class<?>) generic).getName());
            }

        }
    }

    @SuppressWarnings("unchecked")
    private void processWebXmlDescriptor(WebApplicationDescriptor descriptor) {
        StringBuilder webXmlPath = new StringBuilder(descriptor.getInstallationPath()).append(File.separator);
        webXmlPath.append("WEB-INF").append(File.separator).append("web.xml");
        File webXml = new File(webXmlPath.toString());
        if (!webXml.exists()) return;
        try {
            WebXml xml = WebXmlIo.parseWebXmlFromFile(webXml, null);
            WebXmlUtils.addContextParam(xml, CONTEXT_PARAM_NAME.getValue(), descriptor.getContextPath());
            xml.getRootElement().getChildren().add(0, buildListener(xml.getRootElement().getNamespace().getURI()));
            //TODO to be completed
//            WebDescriptor webDescriptor = new WebDescriptor(new FileResource(webXml.toURI().toURL()));
//            webDescriptor.parse();
//            XmlParser.Node root = webDescriptor.getRoot();
//            Iterator<XmlParser.Node> it = root.iterator("resource-ref");
//            while (it.hasNext()) {
//                buildEnvEntry(it.next());
//            }
            WebXmlIo.writeDescriptor(xml, webXml);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Element buildListener(String namespaceURI) {
        Element listener = new Element(WebXmlType.LISTENER, namespaceURI);
        Element listenerClass = new Element("listener-class", namespaceURI);
        listenerClass.setText("com.ejisto.modules.web.ContextListener");
        listener.addContent(listenerClass);
        return listener;
    }

//    private void buildEnvEntry(XmlParser.Node node) {
//        String type = node.getString("res-type", false, true);
//        if (!"javax.sql.DataSource".equals(type)) return;
//        JndiDataSource entry = new JndiDataSource();
//        entry.setName(node.getString("res-ref-name", false, true));
//        entry.setType(type);
//        if (!isAlreadyBound(entry.getName())) JndiDataSourcesRepository.store(entry);
//    }

    private void packageWar(WebApplicationDescriptor session) {
        String libDir = new StringBuilder(session.getInstallationPath()).append(File.separator).append("WEB-INF").append(File.separator).append(
                "lib").append(File.separator).toString();
        File dir = new File(libDir);
        List<CustomObjectFactory> jars = CustomObjectFactoryRepository.getInstance().getCustomObjectFactories();
        for (CustomObjectFactory jar : jars)
            copyFile(System.getProperty(EXTENSIONS_DIR.getValue()) + File.separator + jar.getFileName(), dir);
        copyEjistoLibs(entries, dir);
        String deployablePath = System.getProperty(DEPLOYABLES_DIR.getValue()) + File.separator + session.getWarFile().getName();
        zipDirectory(session.getInstallationPath(), deployablePath);
        session.setDeployablePath(deployablePath);
    }

    protected String getContextPath(String realPath) {
        Matcher matcher = contextExtractor.matcher(realPath.replaceAll(Pattern.quote("\\"), "/"));
        if (matcher.matches()) return matcher.group(1);
        return null;
    }

    private void notifyStart(int numJobs) {
        getView().initProgress(numJobs, getMessage("progress.scan.start"));
    }

    private void notifyJobCompleted(String nextClass) {
        getView().jobCompleted(getMessage("progress.scan.class", nextClass));
    }

    @Override
    public String getTitleKey() {
        return "wizard.applicationscanning.title";
    }

    @Override
    public String getDescriptionKey() {
        return "wizard.applicationscanning.description";
    }

    @Override
    public boolean isBackEnabled() {
        return false;
    }

}
