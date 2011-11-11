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

package com.ejisto.event.listener;

import ch.lambdaj.function.closure.Closure1;
import com.ejisto.core.container.ContainerManager;
import com.ejisto.event.EventManager;
import com.ejisto.event.def.*;
import com.ejisto.event.def.ChangeWebAppContextStatus.WebAppContextStatusCommand;
import com.ejisto.modules.cargo.NotInstalledException;
import com.ejisto.modules.controller.ApplicationInstallerWizardController;
import com.ejisto.modules.dao.WebApplicationDescriptorDao;
import com.ejisto.modules.dao.entities.WebApplicationDescriptor;
import com.ejisto.modules.gui.Application;
import com.ejisto.modules.gui.components.helper.CallbackAction;
import com.ejisto.modules.repository.ClassPoolRepository;
import com.ejisto.modules.repository.MockedFieldsRepository;
import com.ejisto.modules.repository.WebApplicationRepository;
import com.ejisto.util.IOUtils;
import javassist.ClassPool;
import javassist.LoaderClassPath;
import org.apache.log4j.Logger;
import org.springframework.context.ApplicationListener;
import org.springframework.util.Assert;

import javax.annotation.Resource;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URLClassLoader;
import java.util.regex.Pattern;

import static ch.lambdaj.Lambda.forEach;
import static ch.lambdaj.Lambda.var;
import static com.ejisto.constants.StringConstants.*;
import static com.ejisto.util.GuiUtils.*;
import static com.ejisto.util.IOUtils.guessWebApplicationUri;

public class WebApplicationLoader implements ApplicationListener<LoadWebApplication> {

    private static final Logger logger = Logger.getLogger(WebApplicationLoader.class);
    private static final Pattern SPLIT_PATTERN = Pattern.compile(Pattern.quote(CONTEXT_PREFIX_SEPARATOR.getValue()));
    @Resource private Application application;
    @Resource private EventManager eventManager;
    @Resource private MockedFieldsRepository mockedFieldsRepository;
    @Resource private WebApplicationDescriptorDao webApplicationDescriptorDao;
    @Resource private ContainerManager containerManager;
    @Resource private WebApplicationRepository webApplicationRepository;

    private Closure1<ActionEvent> callNotifyCommand;

    @Override
    public void onApplicationEvent(LoadWebApplication event) {
        try {
            if (event.loadStored()) loadExistingWebApplications();
            else installNewWebApplication();
        } catch (NotInstalledException e) {
            showErrorMessage(application, getMessage("container.installation.required"));
            eventManager.publishEvent(new InstallContainer(this, e.getId(), false));
        } catch (Exception e) {
            eventManager.publishEvent(new ApplicationError(this, ApplicationError.Priority.HIGH, e));
            logger.error(e.getMessage(), e);
        }
    }

    private void installNewWebApplication() throws NotInstalledException {
        ApplicationInstallerWizardController controller = new ApplicationInstallerWizardController(application,
                                                                                                   containerManager.getDefaultHome());
        if (!controller.showWizard()) return;
        WebApplicationDescriptor webApplicationDescriptor = controller.getWebApplicationDescriptor();
        forEach(webApplicationDescriptor.getFields()).setContextPath(webApplicationDescriptor.getContextPath());
        forEach(webApplicationDescriptor.getModifiedFields()).setActive(true);
        mockedFieldsRepository.deleteContext(webApplicationDescriptor.getContextPath());
        mockedFieldsRepository.insert(webApplicationDescriptor.getFields());

        deployWebApp(webApplicationDescriptor);
        saveWebAppDescriptor(webApplicationDescriptor);
        startBrowser(webApplicationDescriptor);
        eventManager.publishEvent(
                new ApplicationDeployed(this, webApplicationDescriptor.getContextPath(),
                                        webApplicationDescriptor.getContainerId()));
        eventManager.publishEvent(
                new StatusBarMessage(this, getMessage("statusbar.installation.successful.message",
                                                      webApplicationDescriptor.getContextPath()),
                                     false));
    }

    private void loadExistingWebApplications() {
        loadWebAppDescriptors();
    }

    private void deployWebApp(WebApplicationDescriptor webApplicationDescriptor) throws NotInstalledException {
        if (webApplicationDescriptor == null) return;
        containerManager.deployToDefaultContainer(webApplicationDescriptor);
        //undeployExistingWebapp(webApplicationDescriptor.getContextPath(), false);
        //bindAllDataSources(classLoader, webApplicationDescriptor);
        registerActions(webApplicationDescriptor);
    }
//TODO implement DataSources binding
//    private void bindAllDataSources(EjistoClassLoader context, WebApplicationDescriptor descriptor) throws Exception {
//        if (!descriptor.containsDataSources()) return;
//        Set<String> extraClasspath = new HashSet<String>();
//        String libDir = System.getProperty(StringConstants.LIB_DIR.getValue());
//        for (JndiDataSource dataSourceEnvEntry : descriptor.getDataSources()) {
//            extraClasspath.add(new File(libDir, dataSourceEnvEntry.getDriverJarPath()).getAbsolutePath());
//        }
//        context.addExtraEntries(extraClasspath);
//        JndiUtils.bindResources(descriptor.getDataSources());
//    }

    private void registerActions(WebApplicationDescriptor descriptor) {
        if (callNotifyCommand == null) {
            callNotifyCommand = new Closure1<ActionEvent>() {{
                of(WebApplicationLoader.this).notifyCommand(var(ActionEvent.class));
            }};
        }
        putAction(createAction(
                buildCommand(START_CONTEXT_PREFIX, descriptor.getContainerId(), descriptor.getContextPath()),
                callNotifyCommand,
                getMessage("webapp.context.start.icon"), true));
        putAction(createAction(
                buildCommand(STOP_CONTEXT_PREFIX, descriptor.getContainerId(), descriptor.getContextPath()),
                callNotifyCommand,
                getMessage("webapp.context.stop.icon"), false));
        putAction(createAction(
                buildCommand(DELETE_CONTEXT_PREFIX, descriptor.getContainerId(), descriptor.getContextPath()),
                callNotifyCommand,
                getMessage("webapp.context.delete.icon"), true));
    }

    private CallbackAction createAction(String command, Closure1<ActionEvent> callback, String iconKey, boolean enabled) {
        CallbackAction action = new CallbackAction(command, command, callback);
        if (iconKey != null) action.setIcon(new ImageIcon(getClass().getResource(iconKey)));
        action.setEnabled(enabled);
        return action;
    }

    void notifyCommand(ActionEvent event) {
        try {
            String[] command = SPLIT_PATTERN.split(event.getActionCommand());
            Assert.state(command.length == 3);
            WebAppContextStatusCommand statusCommand = WebAppContextStatusCommand.fromString(command[1]);
            switch (statusCommand) {
                case START:
                    startWebapp(command[0], command[2]);
                    break;
                case STOP:
                    stopWebapp(command[0], command[2]);
                    break;
                case DELETE:
                    if (showWarning(application, "webapp.context.delete.warning", command[2]))
                        undeployExistingWebapp(command[0], command[2]);
                    break;
                default:
                    break;
            }
            eventManager.publishEvent(new ApplicationDeployed(this, command[2], command[0]));
        } catch (Exception e) {
            eventManager.publishEvent(new ApplicationError(this, ApplicationError.Priority.HIGH, e));
        }
    }

    private void undeployExistingWebapp(String serverId, String contextPath) throws Exception {
        logger.info("undeploying webapp " + contextPath);
        if (containerManager.undeployFromDefaultContainer(contextPath))
            logger.info("webapp " + contextPath + " undeployed");
    }

    private void startWebapp(String serverId, String contextPath) throws Exception {
        logger.info("starting webapp " + contextPath);
        if (containerManager.startWebApplicationOnDefaultServer(contextPath))
            logger.info("started webapp " + contextPath);
    }

    private void stopWebapp(String serverId, String contextPath) throws Exception {
        logger.info("stopping webapp " + contextPath);
        if (containerManager.stopWebApplicationOnDefaultServer(contextPath))
            logger.info("stopped webapp " + contextPath);
    }

    private void modifyActionState(String contextPath, boolean start) {
        getAction(new StringBuilder(START_CONTEXT_PREFIX.getValue()).append(contextPath).toString()).setEnabled(!start);
        getAction(new StringBuilder(STOP_CONTEXT_PREFIX.getValue()).append(contextPath).toString()).setEnabled(start);
    }

    private void saveWebAppDescriptor(WebApplicationDescriptor webApplicationDescriptor) {
        try {
            webApplicationDescriptorDao.insert(webApplicationDescriptor);
        } catch (Exception e) {
            eventManager.publishEvent(new ApplicationError(this, ApplicationError.Priority.HIGH, e));
            logger.error("error saving webappdescriptor", e);
        }
    }

    private void loadWebAppDescriptors() {
        try {
            for (WebApplicationDescriptor descriptor : webApplicationDescriptorDao.loadAll()) {
                deployWebApp(descriptor);
                registerClassPool(descriptor);
            }
        } catch (Exception e) {
            logger.error("unable to load webapp descriptor: ", e);
        }
    }

    private void registerClassPool(WebApplicationDescriptor descriptor) throws MalformedURLException {
        ClassPool cp = ClassPoolRepository.getRegisteredClassPool(descriptor.getContextPath());
        cp.appendClassPath(new LoaderClassPath(Thread.currentThread().getContextClassLoader()));
        cp.appendClassPath(new LoaderClassPath(new URLClassLoader(IOUtils.toUrlArray(descriptor))));
    }

    private void startBrowser(WebApplicationDescriptor descriptor) {
        //thanks to sun, browser is not available on kde http://bugs.sun.com/view_bug.do?bug_id=6486393
        if (!Desktop.isDesktopSupported() || !containerManager.isServerRunning()) return;
        try {
            Desktop.getDesktop().browse(URI.create(guessWebApplicationUri(descriptor)));
        } catch (IOException e) {
            logger.error("unable to open system browser", e);
        }
    }
}
