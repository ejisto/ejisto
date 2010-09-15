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

package com.ejisto.event.listener;

import static ch.lambdaj.Lambda.forEach;
import static ch.lambdaj.Lambda.var;
import static com.ejisto.constants.StringConstants.CONTEXT_PREFIX_SEPARATOR;
import static com.ejisto.constants.StringConstants.DELETE_CONTEXT_PREFIX;
import static com.ejisto.constants.StringConstants.DESCRIPTOR_DIR;
import static com.ejisto.constants.StringConstants.START_CONTEXT_PREFIX;
import static com.ejisto.constants.StringConstants.STOP_CONTEXT_PREFIX;
import static com.ejisto.util.GuiUtils.getAction;
import static com.ejisto.util.GuiUtils.getMessage;
import static com.ejisto.util.GuiUtils.putAction;
import static com.ejisto.util.GuiUtils.showWarning;
import static com.ejisto.util.IOUtils.determineWebApplicationUri;
import static com.ejisto.util.IOUtils.readFile;
import static com.ejisto.util.IOUtils.writeFile;

import java.awt.Desktop;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.regex.Pattern;

import javax.annotation.Resource;
import javax.swing.ImageIcon;
import javax.swing.SwingUtilities;

import org.apache.log4j.Logger;
import org.eclipse.jetty.server.HandlerContainer;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.webapp.WebAppContext;
import org.springframework.context.ApplicationListener;
import org.springframework.util.Assert;

import ch.jamme.Marshaller;
import ch.lambdaj.function.closure.Closure1;

import com.ejisto.core.classloading.EjistoClassLoader;
import com.ejisto.core.jetty.WebAppContextRepository;
import com.ejisto.core.jetty.WebApplicationDescriptor;
import com.ejisto.event.EventManager;
import com.ejisto.event.def.ApplicationError;
import com.ejisto.event.def.ChangeWebAppContextStatus.WebAppContextStatusCommand;
import com.ejisto.event.def.LoadWebApplication;
import com.ejisto.modules.controller.ApplicationInstallerWizardController;
import com.ejisto.modules.dao.MockedFieldsDao;
import com.ejisto.modules.dao.entities.MockedField;
import com.ejisto.modules.gui.Application;
import com.ejisto.modules.gui.components.helper.CallbackAction;

public class WebApplicationLoader implements ApplicationListener<LoadWebApplication> {

    private static final Logger logger = Logger.getLogger(WebApplicationLoader.class);
    private static final Pattern SPLIT_PATTERN = Pattern.compile(Pattern.quote(CONTEXT_PREFIX_SEPARATOR.getValue()));
    @Resource
    private Application application;
    @Resource
    private EventManager eventManager;
    @Resource
    private HandlerContainer contexts;
    @Resource
    private MockedFieldsDao mockedFieldsDao;
    @Resource
    private Server jettyServer;
    @Resource
    private Marshaller marshaller;
    @Resource
    private WebAppContextRepository webAppContextRepository;
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
        List<MockedField> fields = webApplicationDescriptor.getModifiedFields();
        forEach(fields).setContextPath(webApplicationDescriptor.getContextPath());
        mockedFieldsDao.deleteContext(webApplicationDescriptor.getContextPath());
        mockedFieldsDao.insert(fields);
        deployWebApp(webApplicationDescriptor);
        saveWebAppDescriptor(webApplicationDescriptor);
        startBrowser(webApplicationDescriptor);
        application.onApplicationDeploy();
    }

    private void loadExistingWebApplications() {
        loadWebAppDescriptors();
    }

    private void deployWebApp(WebApplicationDescriptor webApplicationDescriptor) {
        if (webApplicationDescriptor == null) return;
        try {
            undeployExistingWebapp(webApplicationDescriptor.getContextPath(), false);
            WebAppContext context = new WebAppContext(contexts, webApplicationDescriptor.getInstallationPath(), webApplicationDescriptor.getContextPath());
            context.setResourceBase(webApplicationDescriptor.getInstallationPath());
            EjistoClassLoader classLoader = new EjistoClassLoader(webApplicationDescriptor.getInstallationPath(), context);
            context.setClassLoader(classLoader);
            context.setParentLoaderPriority(false);
            context.start();
            webAppContextRepository.registerWebAppContext(context);
            registerActions(context);
            
        } catch (Exception e) {
            eventManager.publishEvent(new ApplicationError(this, ApplicationError.Priority.HIGH, e));
            logger.error(e.getMessage(), e);
        }
    }
    
    private void registerActions(WebAppContext context) {
        if(callNotifyCommand == null) {
            callNotifyCommand = new Closure1<ActionEvent>() {{ of(WebApplicationLoader.this).notifyCommand(var(ActionEvent.class)); }};
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
        if(iconKey != null) action.setIcon(new ImageIcon(getClass().getResource(iconKey)));
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
                if(showWarning(application, "jettycontrol.context.delete.warning", command[1])) 
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
        if(webAppContextRepository.containsWebAppContext(contextPath)) {
            logger.info("undeploying webapp "+contextPath);
            WebAppContext webAppContext = webAppContextRepository.getWebAppContext(contextPath);
            webAppContext.stop();
            webAppContext.destroy();
            webAppContextRepository.unregisterWebAppContext(webAppContext);
            if(deleteDescriptor) {
                File f = new File(System.getProperty(DESCRIPTOR_DIR.getValue()) + contextPath + ".xml");
                if(f.exists()) f.delete();
            }
            logger.info("webapp "+contextPath+ " undeployed");
        }
    }
    
    private void startWebapp(String contextPath) throws Exception {
        if(webAppContextRepository.containsWebAppContext(contextPath)) {
            logger.info("starting webapp "+contextPath);
            WebAppContext webAppContext = webAppContextRepository.getWebAppContext(contextPath);
            webAppContext.start();
            logger.info("started webapp "+contextPath);
            modifyActionState(contextPath, true);
        }
    }
    
    private void stopWebapp(String contextPath) throws Exception {
        if(webAppContextRepository.containsWebAppContext(contextPath)) {
            logger.info("stopping webapp "+contextPath);
            WebAppContext webAppContext = webAppContextRepository.getWebAppContext(contextPath);
            webAppContext.stop();
            logger.info("stopped webapp "+contextPath);
            modifyActionState(contextPath, false);
        }
    }
    
    private void modifyActionState(String contextPath, boolean start) {
        getAction(new StringBuilder(START_CONTEXT_PREFIX.getValue()).append(contextPath).toString()).setEnabled(!start);
        getAction(new StringBuilder(STOP_CONTEXT_PREFIX.getValue()).append(contextPath).toString()).setEnabled(start);
    }

    private void saveWebAppDescriptor(WebApplicationDescriptor webApplicationDescriptor) {
        try {
            String xml = marshaller.marshall(webApplicationDescriptor);
            writeFile(xml.getBytes(), System.getProperty(DESCRIPTOR_DIR.getValue()) + webApplicationDescriptor.getContextPath() + ".xml");
        } catch (Exception e) {
            eventManager.publishEvent(new ApplicationError(this, ApplicationError.Priority.HIGH, e));
            logger.error("error saving webappdescriptor", e);
        }
    }

    private void loadWebAppDescriptors() {
        try {
            File dir = new File(System.getProperty(DESCRIPTOR_DIR.getValue()));
            for (File xml : dir.listFiles()) {
                deployWebApp((WebApplicationDescriptor)marshaller.unmarshall(new String(readFile(xml))));
            }
        } catch (IOException e) {
            logger.error("unable to load webapp descriptor: ", e);
        }
    }

    private void startBrowser(WebApplicationDescriptor descriptor) {
        //thanks to sun, browser is not available on kde http://bugs.sun.com/view_bug.do?bug_id=6486393
        if (!Desktop.isDesktopSupported() || !jettyServer.isRunning()) return;
        try {
            Desktop.getDesktop().browse(URI.create(determineWebApplicationUri(descriptor)));
        } catch (IOException e) {
            logger.error("unable to open system browser", e);
        }
    }

}
