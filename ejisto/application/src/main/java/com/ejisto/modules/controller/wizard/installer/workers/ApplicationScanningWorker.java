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

import com.ejisto.core.classloading.decorator.MockedFieldDecorator;
import com.ejisto.modules.controller.wizard.installer.ApplicationScanningController;
import com.ejisto.modules.dao.entities.CustomObjectFactory;
import com.ejisto.modules.dao.entities.MockedField;
import com.ejisto.modules.dao.entities.WebApplicationDescriptor;
import com.ejisto.modules.executor.ErrorDescriptor;
import com.ejisto.modules.executor.GuiTask;
import com.ejisto.modules.repository.ClassPoolRepository;
import com.ejisto.modules.repository.CustomObjectFactoryRepository;
import com.ejisto.modules.repository.MockedFieldsRepository;
import javassist.*;
import lombok.extern.log4j.Log4j;
import org.codehaus.cargo.module.DescriptorElement;
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
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static ch.lambdaj.Lambda.join;
import static com.ejisto.constants.StringConstants.*;
import static com.ejisto.modules.executor.ErrorDescriptor.Category.ERROR;
import static com.ejisto.modules.executor.ErrorDescriptor.Category.WARN;
import static com.ejisto.modules.executor.ProgressDescriptor.ProgressState.COMPLETED;
import static com.ejisto.modules.executor.ProgressDescriptor.ProgressState.INDETERMINATE;
import static com.ejisto.util.GuiUtils.getMessage;
import static com.ejisto.util.IOUtils.*;

/**
 * Created by IntelliJ IDEA.
 * User: celestino
 * Date: 11/25/11
 * Time: 5:45 PM
 */
@Log4j
public class ApplicationScanningWorker extends GuiTask<Void> {
    private static final Pattern contextExtractor = Pattern.compile("^[/a-zA-Z0-9\\s\\W]+(/.+?)/?$");
    private static final String[] entries = {"derbyclient", "derbynet", "ejisto-core", "hamcrest", "javassist", "lambdaj", "objenesis", "ognl", "spring", "cglib", "commons", "asm"};
    private WebApplicationDescriptor session;
    private String containerHome;

    public ApplicationScanningWorker(ApplicationScanningController controller, String containerHome) {
        super();
        this.session = controller.getSession();
        this.containerHome = containerHome;
        addPropertyChangeListener(controller);
    }

    @Override
    protected Void doInBackground() throws Exception {
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
        notifyJobCompleted(COMPLETED, getMessage("progress.scan.end"));
    }

    String getContextPath(String realPath) {
        Matcher matcher = contextExtractor.matcher(realPath.replaceAll(Pattern.quote("\\"), "/"));
        if (matcher.matches()) return matcher.group(1);
        return null;
    }

    private void loadAllClasses(Collection<String> classNames, WebApplicationDescriptor descriptor) throws Exception {
        firePropertyChange("startProgress", 0, classNames.size());
        descriptor.deleteAllFields();
        URL[] descriptorEntries = toUrlArray(descriptor);
        ClassLoader loader = new URLClassLoader(
                addServerLibs(descriptorEntries, containerHome + File.separator + "lib"));
        String contextPath = descriptor.getContextPath();
        ClassPool cp = ClassPoolRepository.getRegisteredClassPool(contextPath);
        cp.appendClassPath(new LoaderClassPath(loader));
        int progressCounter = 0;
        for (String className : classNames) {
            notifyJobCompleted(++progressCounter, className);
            loadClass(className, cp, descriptor, loader);
        }
        log.info("just finished processing " + descriptor.getInstallationPath());
    }

    private void loadClass(String className, ClassPool cp, WebApplicationDescriptor descriptor, ClassLoader loader) {
        try {
            CtClass clazz = cp.get(className);
            fillMockedFields(clazz, descriptor, loader);
            clazz.detach();
        } catch (Throwable e) {
            addErrorDescriptor(buildErrorDescriptor(e));
        }
    }

    private void fillMockedFields(CtClass clazz, WebApplicationDescriptor descriptor, ClassLoader loader) throws NotFoundException {
        MockedField mockedField;
        try {
            List<MockedField> mockedFields = MockedFieldsRepository.getInstance().load(descriptor.getContextPath(),
                                                                                       clazz.getName());
            for (CtField field : clazz.getDeclaredFields()) {
                mockedField = new MockedFieldDecorator();
                mockedField.setContextPath(descriptor.getContextPath());
                mockedField.setClassName(clazz.getName());
                mockedField.setFieldName(field.getName());
                mockedField.setFieldType(field.getType().getName());
                parseGenerics(clazz, field, mockedField, loader);
                int index = mockedFields.indexOf(mockedField);
                if (index > -1) mockedField.copyFrom(mockedFields.get(index));
                descriptor.addField(mockedField);
            }
            CtClass zuperclazz = clazz.getSuperclass();
            if (!zuperclazz.getName().startsWith("java"))
                fillMockedFields(zuperclazz, descriptor, loader);
        } catch (Exception e) {
            addErrorDescriptor(buildErrorDescriptor(e));
        }
    }

    private ErrorDescriptor buildErrorDescriptor(Throwable e) {
        Class<?> c = e.getClass();
        boolean classIssue = NotFoundException.class.isAssignableFrom(c) ||
                NoClassDefFoundError.class.isAssignableFrom(c);
        return new ErrorDescriptor(e, classIssue ? WARN : ERROR);
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
            log.error(e.getMessage(), e);
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
    private void processWebXmlDescriptor(WebApplicationDescriptor descriptor) throws Exception {
        notifyJobCompleted(INDETERMINATE, getMessage("wizard.resource.web.xml.processing"));
        StringBuilder webXmlPath = new StringBuilder(descriptor.getInstallationPath()).append(File.separator);
        webXmlPath.append("WEB-INF").append(File.separator).append("web.xml");
        File webXml = new File(webXmlPath.toString());
        if (!webXml.exists()) return;
        WebXml xml = WebXmlIo.parseWebXmlFromFile(webXml, null);
        DescriptorElement param = (DescriptorElement) WebXmlUtils.getContextParam(xml, CONTEXT_PARAM_NAME.getValue());
        if (param != null) return;
        WebXmlUtils.addContextParam(xml, CONTEXT_PARAM_NAME.getValue(), descriptor.getContextPath());
        xml.getRootElement().getChildren().add(0, buildListener(xml.getRootElement().getNamespace().getURI()));
        WebXmlIo.writeDescriptor(xml, webXml);
    }

    private Element buildListener(String namespaceURI) {
        Element listener = new Element(WebXmlType.LISTENER, namespaceURI);
        Element listenerClass = new Element("listener-class", namespaceURI);
        listenerClass.setText("com.ejisto.modules.web.ContextListener");
        listener.addContent(listenerClass);
        return listener;
    }

    private void packageWar(WebApplicationDescriptor session) throws Exception {
        notifyJobCompleted(0, getMessage("wizard.resource.war.packaging"));
        String libDir = new StringBuilder(session.getInstallationPath()).append(File.separator).append(
                "WEB-INF").append(File.separator).append(
                "lib").append(File.separator).toString();
        File dir = new File(libDir);
        List<CustomObjectFactory> jars = CustomObjectFactoryRepository.getInstance().getCustomObjectFactories();
        for (CustomObjectFactory jar : jars)
            copyFile(System.getProperty(EXTENSIONS_DIR.getValue()) + File.separator + jar.getFileName(), dir);
        copyEjistoLibs(entries, dir);
        String deployablePath = System.getProperty(
                DEPLOYABLES_DIR.getValue()) + File.separator + session.getWarFile().getName();
        zipDirectory(session.getInstallationPath(), deployablePath);
        session.setDeployablePath(deployablePath);
    }

    private WebApplicationDescriptor getSession() {
        return session;
    }


}
