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

import com.ejisto.modules.controller.wizard.installer.ApplicationScanningController;
import com.ejisto.modules.dao.entities.CustomObjectFactory;
import com.ejisto.modules.dao.entities.MockedField;
import com.ejisto.modules.dao.entities.WebApplicationDescriptor;
import com.ejisto.modules.executor.ErrorDescriptor;
import com.ejisto.modules.executor.GuiTask;
import com.ejisto.modules.repository.ClassPoolRepository;
import com.ejisto.modules.repository.CustomObjectFactoryRepository;
import javassist.ClassPool;
import javassist.LoaderClassPath;
import lombok.extern.log4j.Log4j;
import org.codehaus.cargo.module.DescriptorElement;
import org.codehaus.cargo.module.webapp.WebXml;
import org.codehaus.cargo.module.webapp.WebXmlIo;
import org.codehaus.cargo.module.webapp.WebXmlType;
import org.codehaus.cargo.module.webapp.WebXmlUtils;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.springframework.util.Assert;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.ejisto.constants.StringConstants.*;
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
public class ApplicationScanningWorker extends GuiTask<Void> implements ProgressListener {
    private static final Pattern contextExtractor = Pattern.compile("^[/a-zA-Z0-9\\s\\W]+(/.+?)/?$");
    private static final String[] entries = {"ejisto-core", "hamcrest", "javassist", "lambdaj", "objenesis", "ognl", "cglib", "commons", "asm", "jackson"};
    private static final ForkJoinPool forkJoinPool = new ForkJoinPool();
    private final AtomicInteger counter = new AtomicInteger();
    private WebApplicationDescriptor session;
    private String containerHome;

    public ApplicationScanningWorker(ApplicationScanningController controller, String containerHome) {
        super();
        this.session = controller.getSession();
        this.containerHome = containerHome;
        addPropertyChangeListener(controller);
    }

    @Override
    protected Void internalDoInBackground() throws JDOMException, IOException, InvocationTargetException, InterruptedException {
        processWebApplication();
        return null;
    }

    private void processWebApplication() throws IOException, JDOMException, InvocationTargetException, InterruptedException {
        Assert.notNull(getSession().getInstallationPath());
        String basePath = getSession().getInstallationPath();
        getSession().setContextPath(getContextPath(basePath));
        getSession().setClassPathElements(getClasspathEntries(basePath));
        loadAllClasses(findAllWebApplicationClasses(basePath, getSession()), getSession());
        processWebXmlDescriptor(getSession());
        createDeployable(getSession());
        notifyJobCompleted(COMPLETED, getMessage("progress.scan.end"));
    }

    String getContextPath(String realPath) {
        Matcher matcher = contextExtractor.matcher(realPath.replaceAll(Pattern.quote("\\"), "/"));
        if (matcher.matches()) {
            return matcher.group(1);
        }
        return null;
    }

    private void loadAllClasses(Collection<String> classNames, WebApplicationDescriptor descriptor) throws MalformedURLException {
        firePropertyChange("startProgress", 0, classNames.size());
        descriptor.deleteAllFields();
        URL[] descriptorEntries = toUrlArray(descriptor);
        ClassLoader loader = new URLClassLoader(
                addServerLibs(descriptorEntries, containerHome + File.separator + "lib"));
        String contextPath = descriptor.getContextPath();
        ClassPool cp = ClassPoolRepository.getRegisteredClassPool(contextPath);
        cp.appendClassPath(new LoaderClassPath(loader));
        List<MockedField> fields = forkJoinPool.invoke(
                new LoadClassAction(new ArrayList<>(classNames), loader, descriptor, this));
        for (MockedField field : fields) {
            descriptor.addField(field);
        }
        log.info("just finished processing " + descriptor.getInstallationPath());
    }

    @SuppressWarnings("unchecked")
    private void processWebXmlDescriptor(WebApplicationDescriptor descriptor) throws InvocationTargetException, InterruptedException, JDOMException, IOException {
        notifyJobCompleted(INDETERMINATE, getMessage("wizard.resource.web.xml.processing"));
        StringBuilder webXmlPath = new StringBuilder(descriptor.getInstallationPath()).append(File.separator);
        webXmlPath.append("WEB-INF").append(File.separator).append("web.xml");
        File webXml = new File(webXmlPath.toString());
        if (!webXml.exists()) {
            return;
        }
        WebXml xml = WebXmlIo.parseWebXmlFromFile(webXml, null);
        DescriptorElement param = (DescriptorElement) WebXmlUtils.getContextParam(xml, TARGET_CONTEXT_PATH.getValue());
        if (param != null) {
            return;
        }
        WebXmlUtils.addContextParam(xml, TARGET_CONTEXT_PATH.getValue(), descriptor.getContextPath());
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

    private void createDeployable(WebApplicationDescriptor session) {
        try {
            notifyJobCompleted(0, getMessage("wizard.resource.war.packaging"));
            String libDir = String.format("%sWEB-INF%slib%s", session.getInstallationPath(), File.separator,
                                          File.separator);
            File dir = new File(libDir);
            List<CustomObjectFactory> jars = CustomObjectFactoryRepository.getInstance().getCustomObjectFactories();
            for (CustomObjectFactory jar : jars) {
                copyFile(System.getProperty(EXTENSIONS_DIR.getValue()) + File.separator + jar.getFileName(), dir);
            }
            copyEjistoLibs(entries, dir);
            String deployablePath = System.getProperty(
                    DEPLOYABLES_DIR.getValue()) + File.separator + getFilenameWithoutExt(
                    session.getWarFile()) + File.separator;
            deleteFile(deployablePath);
            copyDirContent(session.getInstallationPath(), deployablePath);
            session.setDeployablePath(deployablePath);
        } catch (InterruptedException e) {
            log.error("got interruptedException: ", e);
        } catch (InvocationTargetException e) {
            log.error("got invocationTargetException", e);
        }
    }

    private WebApplicationDescriptor getSession() {
        return session;
    }


    @Override
    public void progressChanged(int progress, String message) {
        try {
            notifyJobCompleted(counter.addAndGet(progress), message);
        } catch (Exception e) {
            log.error("an exception occurred while dispatching progress changed event", e);
        }
    }

    @Override
    public void errorOccurred(ErrorDescriptor errorDescriptor) {
        addErrorDescriptor(errorDescriptor);
    }
}
