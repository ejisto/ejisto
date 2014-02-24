/*
 * Ejisto, a powerful developer assistant
 *
 * Copyright (C) 2010-2013 Celestino Bellone
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

import com.ejisto.core.container.ContainerManager;
import com.ejisto.event.ApplicationEventDispatcher;
import com.ejisto.event.ApplicationListener;
import com.ejisto.event.EventManager;
import com.ejisto.event.def.*;
import com.ejisto.event.def.ChangeWebAppContextStatus.WebAppContextStatusCommand;
import com.ejisto.modules.cargo.NotInstalledException;
import com.ejisto.modules.controller.ApplicationInstallerWizardController;
import com.ejisto.modules.dao.entities.WebApplicationDescriptor;
import com.ejisto.modules.dao.local.LocalWebApplicationDescriptorDao;
import com.ejisto.modules.executor.TaskManager;
import com.ejisto.modules.gui.Application;
import com.ejisto.modules.gui.components.helper.CallbackAction;
import com.ejisto.modules.repository.ClassPoolRepository;
import com.ejisto.modules.repository.CustomObjectFactoryRepository;
import com.ejisto.modules.repository.MockedFieldsRepository;
import com.ejisto.modules.repository.SettingsRepository;
import com.ejisto.util.GuiUtils;
import com.ejisto.util.IOUtils;
import javassist.ClassPool;
import javassist.LoaderClassPath;
import lombok.extern.log4j.Log4j;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.regex.Pattern;

import static com.ejisto.constants.StringConstants.*;
import static com.ejisto.util.GuiUtils.*;
import static com.ejisto.util.IOUtils.guessWebApplicationUri;

@Log4j
public class WebApplicationLoader implements ApplicationListener<LoadWebApplication> {

    private static final Pattern SPLIT_PATTERN = Pattern.compile(Pattern.quote(CONTEXT_PREFIX_SEPARATOR.getValue()));
    private final Application application;
    private final EventManager eventManager;
    private final MockedFieldsRepository mockedFieldsRepository;
    private final LocalWebApplicationDescriptorDao webApplicationDescriptorDao;
    private final ContainerManager containerManager;
    private final CustomObjectFactoryRepository customObjectFactoryRepository;
    private final SettingsRepository settingsRepository;
    private final TaskManager taskManager;
    private final ApplicationEventDispatcher eventDispatcher;

    public WebApplicationLoader(Application application,
                                EventManager eventManager,
                                MockedFieldsRepository mockedFieldsRepository,
                                LocalWebApplicationDescriptorDao webApplicationDescriptorDao,
                                ContainerManager containerManager,
                                CustomObjectFactoryRepository customObjectFactoryRepository,
                                SettingsRepository settingsRepository,
                                TaskManager taskManager,
                                ApplicationEventDispatcher eventDispatcher) {
        this.application = application;
        this.eventManager = eventManager;
        this.mockedFieldsRepository = mockedFieldsRepository;
        this.webApplicationDescriptorDao = webApplicationDescriptorDao;
        this.containerManager = containerManager;
        this.customObjectFactoryRepository = customObjectFactoryRepository;
        this.settingsRepository = settingsRepository;
        this.taskManager = taskManager;
        this.eventDispatcher = eventDispatcher;
    }

    @Override
    public void onApplicationEvent(LoadWebApplication event) {
        try {
            if (event.loadStored()) {
                loadExistingWebApplications();
            } else {
                installNewWebApplication();
            }
        } catch (NotInstalledException e) {
            showErrorMessage(application, getMessage("container.installation.required"));
            eventManager.publishEvent(new InstallContainer(this, e.getId(), false));
        } catch (Exception e) {
            eventManager.publishEvent(new ApplicationError(this, ApplicationError.Priority.HIGH, e));
            log.error(e.getMessage(), e);
        }
    }

    @Override
    public Class<LoadWebApplication> getTargetEventType() {
        return LoadWebApplication.class;
    }

    private void installNewWebApplication() throws NotInstalledException {
        ApplicationInstallerWizardController controller = new ApplicationInstallerWizardController(application,
                                                                                                   containerManager.getDefaultHome(),
                                                                                                   mockedFieldsRepository,
                                                                                                   customObjectFactoryRepository,
                                                                                                   settingsRepository,
                                                                                                   taskManager,
                                                                                                   eventDispatcher);
        if (!controller.showWizard()) {
            return;
        }
        WebApplicationDescriptor webApplicationDescriptor = controller.getWebApplicationDescriptor();
        webApplicationDescriptor.getFields().stream().forEach(descriptor -> descriptor.setContextPath(webApplicationDescriptor.getContextPath()));
        webApplicationDescriptor.getModifiedFields().stream().forEach(descriptor -> descriptor.setActive(true));
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
        if (webApplicationDescriptor == null) {
            return;
        }
        containerManager.deployToDefaultContainer(webApplicationDescriptor);
        registerActions(webApplicationDescriptor);
    }

    private void registerActions(WebApplicationDescriptor descriptor) {
        putAction(createAction(
                buildCommand(START_CONTEXT_PREFIX, descriptor.getContainerId(), descriptor.getContextPath()),
                this::notifyCommand,
                getMessage("webapp.context.start.icon"), true));
        putAction(createAction(
                buildCommand(STOP_CONTEXT_PREFIX, descriptor.getContainerId(), descriptor.getContextPath()),
                this::notifyCommand,
                getMessage("webapp.context.stop.icon"), false));
        putAction(createAction(
                buildCommand(DELETE_CONTEXT_PREFIX, descriptor.getContainerId(), descriptor.getContextPath()),
                this::notifyCommand,
                getMessage("webapp.context.delete.icon"), true));
    }

    private CallbackAction createAction(String command, Consumer<ActionEvent> callback, String iconKey, boolean enabled) {
        CallbackAction action = new CallbackAction(command, command, callback, null);
        if (iconKey != null) {
            action.setIcon(new ImageIcon(getClass().getResource(iconKey)));
        }
        action.setEnabled(enabled);
        return action;
    }

    void notifyCommand(ActionEvent event) {
        try {
            String[] command = SPLIT_PATTERN.split(event.getActionCommand());
            if (command.length != 3) {
                throw new IllegalArgumentException(event.getActionCommand());
            }
            WebAppContextStatusCommand statusCommand = WebAppContextStatusCommand.fromString(command[1])
                    .orElseThrow(IllegalArgumentException::new);
            switch (statusCommand) {
                case START:
                    startWebapp(command[0], command[2]);
                    break;
                case STOP:
                    stopWebapp(command[0], command[2]);
                    break;
                case DELETE:
                    if (showWarning(application, "webapp.context.delete.warning", command[2])) {
                        undeployExistingWebapp(command[0], command[2]);
                    }
                    break;
                default:
                    break;
            }
            eventManager.publishEvent(new ApplicationDeployed(this, command[2], command[0]));
        } catch (Exception e) {
            eventManager.publishEvent(new ApplicationError(this, ApplicationError.Priority.HIGH, e));
        }
    }

    private void undeployExistingWebapp(String serverId, String contextPath) throws NotInstalledException {
        log.info("undeploying webapp " + contextPath);
        if (containerManager.undeployFromDefaultContainer(contextPath)) {
            mockedFieldsRepository.deleteContext(contextPath);
            IOUtils.deleteFile(Paths.get(System.getProperty(DEPLOYABLES_DIR.getValue()), contextPath).toFile());
            log.info("webapp " + contextPath + " undeployed");
        }
    }

    private void startWebapp(String serverId, String contextPath) throws NotInstalledException {
        log.info("starting webapp " + contextPath);
        if (containerManager.startWebApplicationOnDefaultServer(contextPath)) {
            log.info("started webapp " + contextPath);
        }
    }

    private void stopWebapp(String serverId, String contextPath) throws NotInstalledException {
        log.info("stopping webapp " + contextPath);
        if (containerManager.stopWebApplicationOnDefaultServer(contextPath)) {
            log.info("stopped webapp " + contextPath);
        }
    }

    private void saveWebAppDescriptor(WebApplicationDescriptor webApplicationDescriptor) {
        try {
            webApplicationDescriptorDao.insert(webApplicationDescriptor);
        } catch (Exception e) {
            eventManager.publishEvent(new ApplicationError(this, ApplicationError.Priority.HIGH, e));
            log.error("error saving webappdescriptor", e);
        }
    }

    private void loadWebAppDescriptors() {
        try {
            webApplicationDescriptorDao.loadAll().forEach(descriptor -> {
                try {
                    deployWebApp(descriptor);
                    registerClassPool(descriptor);
                } catch (NotInstalledException | MalformedURLException e) {
                    log.error("unable to load webapp descriptor: ", e);
                }
            });
        } catch (Exception e) {
            log.error("unable to load webapp descriptor: ", e);
        }
    }

    private void registerClassPool(WebApplicationDescriptor descriptor) throws MalformedURLException {
        ClassPool cp = ClassPoolRepository.getRegisteredClassPool(descriptor.getContextPath());
        cp.appendClassPath(new LoaderClassPath(Thread.currentThread().getContextClassLoader()));
        cp.appendClassPath(new LoaderClassPath(new URLClassLoader(IOUtils.toUrlArray(descriptor))));
    }

    private void startBrowser(WebApplicationDescriptor descriptor) throws NotInstalledException {
        //thanks to sun, browser is not available on kde http://bugs.sun.com/view_bug.do?bug_id=6486393
        if (!Desktop.isDesktopSupported() || !containerManager.isServerRunning()) {
            return;
        }
        try {
            Desktop.getDesktop().browse(URI.create(guessWebApplicationUri(descriptor, settingsRepository)));
        } catch (IOException e) {
            log.error("unable to open system browser", e);
        }
    }
}
