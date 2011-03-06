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
import com.ejisto.constants.StringConstants;
import com.ejisto.core.classloading.EjistoClassLoader;
import com.ejisto.core.jetty.WebAppContextRepository;
import com.ejisto.event.EventManager;
import com.ejisto.event.def.ApplicationError;
import com.ejisto.event.def.ChangeWebAppContextStatus.WebAppContextStatusCommand;
import com.ejisto.event.def.InstallContainer;
import com.ejisto.event.def.LoadWebApplication;
import com.ejisto.event.def.StatusBarMessage;
import com.ejisto.modules.cargo.CargoManager;
import com.ejisto.modules.cargo.NotInstalledException;
import com.ejisto.modules.controller.ApplicationInstallerWizardController;
import com.ejisto.modules.dao.WebApplicationDescriptorDao;
import com.ejisto.modules.dao.entities.JndiDataSource;
import com.ejisto.modules.dao.entities.MockedField;
import com.ejisto.modules.dao.entities.WebApplicationDescriptor;
import com.ejisto.modules.gui.Application;
import com.ejisto.modules.gui.components.helper.CallbackAction;
import com.ejisto.modules.repository.MockedFieldsRepository;
import com.ejisto.util.JndiUtils;
import org.apache.log4j.Logger;
import org.eclipse.jetty.webapp.WebAppContext;
import org.springframework.context.ApplicationListener;
import org.springframework.util.Assert;

import javax.annotation.Resource;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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
    @Resource private WebAppContextRepository webAppContextRepository;
    @Resource private WebApplicationDescriptorDao webApplicationDescriptorDao;
    @Resource private CargoManager cargoManager;

    private Closure1<ActionEvent> callNotifyCommand;

    @Override
    public void onApplicationEvent(LoadWebApplication event) {
        if (event.loadStored()) loadExistingWebApplications();
        else installNewWebApplication();
    }

    private void installNewWebApplication() {
        ApplicationInstallerWizardController controller = new ApplicationInstallerWizardController(application);
        if (!controller.showWizard()) return;
        WebApplicationDescriptor webApplicationDescriptor = controller.getWebApplicationDescriptor();
        List<MockedField> fields = new ArrayList<MockedField>(webApplicationDescriptor.getModifiedFields());
        forEach(fields).setContextPath(webApplicationDescriptor.getContextPath());
        forEach(fields).setActive(true);
        mockedFieldsRepository.deleteContext(webApplicationDescriptor.getContextPath());
        mockedFieldsRepository.insert(fields);
        deployWebApp(webApplicationDescriptor);
        saveWebAppDescriptor(webApplicationDescriptor);
        startBrowser(webApplicationDescriptor);
        application.onApplicationDeploy();
        eventManager.publishEvent(new StatusBarMessage(this, getMessage("statusbar.installation.successful.message",
                                                                        webApplicationDescriptor.getContextPath()),
                                                       false));
    }

    private void loadExistingWebApplications() {
        loadWebAppDescriptors();
    }

    private void deployWebApp(WebApplicationDescriptor webApplicationDescriptor) {
        if (webApplicationDescriptor == null) return;
        try {
            cargoManager.deployToDefaultContainer(webApplicationDescriptor);
            //undeployExistingWebapp(webApplicationDescriptor.getContextPath(), false);
            //bindAllDataSources(classLoader, webApplicationDescriptor);
//            webAppContextRepository.registerWebAppContext(context);
//            registerActions(context);

        } catch (NotInstalledException e) {
            logger.error("server " + e.getId() + " is not installed.", e);
            eventManager.publishEvent(new InstallContainer(this, e.getId(), false));
        } catch (Exception e) {
            eventManager.publishEvent(new ApplicationError(this, ApplicationError.Priority.HIGH, e));
            logger.error(e.getMessage(), e);
        }
    }

    private void bindAllDataSources(EjistoClassLoader context, WebApplicationDescriptor descriptor) throws Exception {
        if (!descriptor.containsDataSources()) return;
        Set<String> extraClasspath = new HashSet<String>();
        String libDir = System.getProperty(StringConstants.LIB_DIR.getValue());
        for (JndiDataSource dataSourceEnvEntry : descriptor.getDataSources()) {
            extraClasspath.add(new File(libDir, dataSourceEnvEntry.getDriverJarPath()).getAbsolutePath());
        }
        context.addExtraEntries(extraClasspath);
        JndiUtils.bindResources(descriptor.getDataSources());
    }

    private void registerActions(WebAppContext context) {
        if (callNotifyCommand == null) {
            callNotifyCommand = new Closure1<ActionEvent>() {{
                of(WebApplicationLoader.this).notifyCommand(var(ActionEvent.class));
            }};
        }
        String command = new StringBuilder(START_CONTEXT_PREFIX.getValue()).append(context.getContextPath()).toString();
        putAction(createAction(command, callNotifyCommand, getMessage("jettycontrol.context.start.icon"), false));
        command = new StringBuilder(STOP_CONTEXT_PREFIX.getValue()).append(context.getContextPath()).toString();
        putAction(createAction(command, callNotifyCommand, getMessage("jettycontrol.context.stop.icon"), true));
        command = new StringBuilder(DELETE_CONTEXT_PREFIX.getValue()).append(context.getContextPath()).toString();
        putAction(createAction(command, callNotifyCommand, getMessage("jettycontrol.context.delete.icon"), true));
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
            Assert.state(command.length == 2);
            WebAppContextStatusCommand statusCommand = WebAppContextStatusCommand.fromString(command[0]);
            switch (statusCommand) {
                case START:
                    startWebapp(command[1]);
                    break;
                case STOP:
                    stopWebapp(command[1]);
                    break;
                case DELETE:
                    if (showWarning(application, "jettycontrol.context.delete.warning", command[1]))
                        undeployExistingWebapp(command[1], true);
                    break;
                default:
                    break;
            }
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    application.onApplicationDeploy();
                }
            });
        } catch (Exception e) {
            eventManager.publishEvent(new ApplicationError(this, ApplicationError.Priority.HIGH, e));
        }
    }

    private void undeployExistingWebapp(String contextPath, boolean deleteDescriptor) throws Exception {
        if (webAppContextRepository.containsWebAppContext(contextPath)) {
            logger.info("undeploying webapp " + contextPath);
            WebAppContext webAppContext = webAppContextRepository.getWebAppContext(contextPath);
            webAppContext.stop();
            webAppContext.destroy();
            webAppContextRepository.unregisterWebAppContext(webAppContext);
            if (deleteDescriptor) {
                File f = new File(System.getProperty(DESCRIPTOR_DIR.getValue()) + contextPath + ".xml");
                if (f.exists()) f.delete();
            }
            logger.info("webapp " + contextPath + " undeployed");
        }
    }

    private void startWebapp(String contextPath) throws Exception {
        if (webAppContextRepository.containsWebAppContext(contextPath)) {
            logger.info("starting webapp " + contextPath);
            WebAppContext webAppContext = webAppContextRepository.getWebAppContext(contextPath);
            webAppContext.start();
            logger.info("started webapp " + contextPath);
            modifyActionState(contextPath, true);
            eventManager.publishEvent(new StatusBarMessage(this, "", false));
        }
    }

    private void stopWebapp(String contextPath) throws Exception {
        if (webAppContextRepository.containsWebAppContext(contextPath)) {
            logger.info("stopping webapp " + contextPath);
            WebAppContext webAppContext = webAppContextRepository.getWebAppContext(contextPath);
            webAppContext.stop();
            logger.info("stopped webapp " + contextPath);
            modifyActionState(contextPath, false);
        }
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
            }
        } catch (Exception e) {
            logger.error("unable to load webapp descriptor: ", e);
        }
    }

    private void startBrowser(WebApplicationDescriptor descriptor) {
        //thanks to sun, browser is not available on kde http://bugs.sun.com/view_bug.do?bug_id=6486393
        if (!Desktop.isDesktopSupported() || !cargoManager.isServerRunning()) return;
        try {
            Desktop.getDesktop().browse(URI.create(guessWebApplicationUri(descriptor)));
        } catch (IOException e) {
            logger.error("unable to open system browser", e);
        }
    }
}
