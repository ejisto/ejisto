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

package com.ejisto.modules.cargo;

import com.ejisto.constants.StringConstants;
import com.ejisto.core.container.ContainerManager;
import com.ejisto.core.container.WebApplication;
import com.ejisto.event.EventManager;
import com.ejisto.event.def.ApplicationScanRequired;
import com.ejisto.event.def.ChangeServerStatus;
import com.ejisto.modules.cargo.logging.ServerLogger;
import com.ejisto.modules.cargo.util.ContainerInstaller;
import com.ejisto.modules.dao.entities.Container;
import com.ejisto.modules.dao.entities.WebApplicationDescriptor;
import com.ejisto.modules.repository.ContainersRepository;
import com.ejisto.modules.repository.SettingsRepository;
import com.ejisto.modules.repository.WebApplicationRepository;
import com.ejisto.util.ContainerUtils;
import com.ejisto.util.IOUtils;
import lombok.extern.log4j.Log4j;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.codehaus.cargo.container.ContainerType;
import org.codehaus.cargo.container.LocalContainer;
import org.codehaus.cargo.container.configuration.Configuration;
import org.codehaus.cargo.container.configuration.ConfigurationType;
import org.codehaus.cargo.container.configuration.LocalConfiguration;
import org.codehaus.cargo.container.deployable.Deployable;
import org.codehaus.cargo.container.deployable.DeployableType;
import org.codehaus.cargo.container.deployable.WAR;
import org.codehaus.cargo.container.deployer.Deployer;
import org.codehaus.cargo.container.deployer.URLDeployableMonitor;
import org.codehaus.cargo.container.property.GeneralPropertySet;
import org.codehaus.cargo.container.property.ServletPropertySet;
import org.codehaus.cargo.container.spi.AbstractInstalledLocalContainer;
import org.codehaus.cargo.container.tomcat.TomcatPropertySet;
import org.codehaus.cargo.generic.DefaultContainerFactory;
import org.codehaus.cargo.generic.configuration.DefaultConfigurationFactory;
import org.codehaus.cargo.generic.deployable.DefaultDeployableFactory;
import org.codehaus.cargo.generic.deployable.DeployableFactory;
import org.codehaus.cargo.generic.deployer.DefaultDeployerFactory;
import org.codehaus.cargo.generic.deployer.DeployerFactory;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import static ch.lambdaj.Lambda.*;
import static com.ejisto.constants.StringConstants.*;
import static com.ejisto.core.container.WebApplication.Status.STARTED;
import static com.ejisto.util.IOUtils.guessWebApplicationUri;
import static org.hamcrest.Matchers.equalTo;

/**
 * Created by IntelliJ IDEA.
 * User: celestino
 * Date: 2/18/11
 * Time: 7:23 PM
 */
@Log4j
public class CargoManager implements ContainerManager {

    @Resource private ContainersRepository containersRepository;
    @Resource private SettingsRepository settingsRepository;
    @Resource private WebApplicationRepository webApplicationRepository;
    @Resource private EventManager eventManager;

    private final ConcurrentMap<String, AbstractInstalledLocalContainer> installedContainers = new ConcurrentHashMap<>();
    private final ReentrantLock lifeCycleOperationLock = new ReentrantLock();

    @Override
    public String downloadAndInstall(String urlToString, String folder) throws IOException {
        URL url = new URL(urlToString.trim());
        ContainerInstaller installer = new ContainerInstaller(url, folder);
        installer.install();
        containersRepository.registerDefaultContainer(DEFAULT_CONTAINER_ID.getValue(), installer.getHome(),
                                                      settingsRepository.getSettingValue(
                                                              DEFAULT_CONTAINER_DESCRIPTION));
        return installer.getHome();
    }

    @Override
    public boolean isServerRunning() throws NotInstalledException {
        return containersRepository.loadDefault().isRunning();
    }

    @Override
    public boolean startDefault() throws NotInstalledException {
        return start(containersRepository.loadDefault());
    }

    @Override
    public boolean stopDefault() throws NotInstalledException {
        return stop(containersRepository.loadDefault());
    }

    @Override
    public boolean start(Container container) throws NotInstalledException {
        return start(loadContainer(container, true), container);
    }

    @Override
    public boolean stop(Container container) throws NotInstalledException {
        if (!container.isRunning()) {
            return true;
        }
        AbstractInstalledLocalContainer localContainer = loadContainer(container, false);
        boolean owned = false;
        try {
            if (lifeCycleOperationLock.tryLock(30, TimeUnit.SECONDS)) {
                owned = true;
                DeployerFactory deployerFactory = new DefaultDeployerFactory();
                Deployer deployer = deployerFactory.createDeployer(localContainer);
                List<Deployable> deployables = localContainer.getConfiguration().getDeployables();
                for (Deployable deployable : deployables) {
                    deployer.undeploy(deployable);
                }
                localContainer.stop();
                container.setRunningState(false);
                return true;
            }
        } catch (InterruptedException e) {
            log.error("caught InterruptedException", e);
            Thread.currentThread().interrupt();
        } finally {
            if (owned) {
                lifeCycleOperationLock.unlock();
            }
        }
        return false;
    }

    @Override
    public boolean deployToDefaultContainer(WebApplicationDescriptor webApplicationDescriptor) throws NotInstalledException {
        return deploy(webApplicationDescriptor, containersRepository.loadDefault());
    }

    @Override
    public boolean deploy(WebApplicationDescriptor webApplicationDescriptor, Container container) throws NotInstalledException {
        LocalContainer localContainer = loadContainer(container, false);
        boolean started = container.isRunning();
        if (started) {
            eventManager.publishEventAndWait(
                    new ChangeServerStatus(this, container.getId(), ChangeServerStatus.Command.SHUTDOWN));
        }
        eventManager.publishEventAndWait(new ApplicationScanRequired(this, webApplicationDescriptor));
        //Deployable deployable = defaultServerStarted.get() ? hotDeploy(descriptor, container) : staticDeploy(descriptor, container);
        Deployable deployable = staticDeploy(webApplicationDescriptor, localContainer);
        if (deployable == null) {
            return false;
        }
        webApplicationRepository.registerWebApplication(container.getId(),
                                                        new CargoWebApplication(
                                                                webApplicationDescriptor.getContextPath(),
                                                                container.getId(), deployable));
        if (started) {
            eventManager.publishEventAndWait(
                    new ChangeServerStatus(this, container.getId(), ChangeServerStatus.Command.STARTUP));
        }
        return true;
    }

    @Override
    public boolean undeploy(String containerId, String contextPath) throws NotInstalledException {
        return false;
    }

    @Override
    public boolean undeployFromDefaultContainer(String contextPath) throws NotInstalledException {
        Deployable deployable = (Deployable) webApplicationRepository.getRegisteredWebApplication(
                DEFAULT_CONTAINER_ID.getValue(),
                contextPath).getContainerWebApplicationDescriptor();
        undeploy(DEFAULT_CONTAINER_ID.getValue(), contextPath, deployable, loadDefault(false));
        return true;
    }

    @Override
    public boolean startWebApplication(String containerId, String contextPath) throws NotInstalledException {
        return false;
    }

    @Override
    public boolean stopWebApplication(String containerId, String contextPath) throws NotInstalledException {
        return false;
    }

    @Override
    public boolean startWebApplicationOnDefaultServer(String contextPath) throws NotInstalledException {
        return start(DEFAULT_CONTAINER_ID.getValue(), contextPath,
                     getDeployableFromRepository(DEFAULT_CONTAINER_ID.getValue(), contextPath), loadDefault(false));
    }

    @Override
    public boolean stopWebApplicationOnDefaultServer(String contextPath) throws NotInstalledException {
        return stop(DEFAULT_CONTAINER_ID.getValue(), contextPath,
                    getDeployableFromRepository(DEFAULT_CONTAINER_ID.getValue(), contextPath), loadDefault(false));
    }

    @Override
    public String getDefaultHome() throws NotInstalledException {
        return getHome(containersRepository.loadDefault());
    }

    @Override
    public String getHome(Container container) {
        return loadContainer(container, false).getHome();
    }

    private Container buildNewStandaloneContainer() throws NotInstalledException, IOException {
        Container defaultContainer = containersRepository.loadDefault();
        Container container = new Container(true);
        container.setId(UUID.randomUUID().toString());
        container.setCargoId(defaultContainer.getCargoId());
        container.setDescription("Temporary Instance");
        container.setHomeDir(defaultContainer.getHomeDir());
        return container;
    }

    @Override
    public Container startStandaloneInstance(Map<String, String> additionalJavaSystemProperties, List<WebApplicationDescriptor> webApplications) throws NotInstalledException, IOException {
        Container container = buildNewStandaloneContainer();
        Path path = Files.createTempDirectory("standalone").toAbsolutePath();
        Configuration configuration = new DefaultConfigurationFactory().createConfiguration(container.getCargoId(),
                                                                                            ContainerType.INSTALLED,
                                                                                            ConfigurationType.STANDALONE,
                                                                                            path.toString());
        int serverPort = IOUtils.findFirstAvailablePort(9090);
        configuration.setProperty(ServletPropertySet.PORT, String.valueOf(serverPort));
        configuration.setProperty(TomcatPropertySet.AJP_PORT, String.valueOf(IOUtils.findFirstAvailablePort(10000)));
        String existingJvmArgs = configuration.getPropertyValue(GeneralPropertySet.JVMARGS);
        StringBuilder args = new StringBuilder();
        if (StringUtils.isNotBlank(existingJvmArgs)) {
            args.append(existingJvmArgs);
        }
        for (String propertyName : additionalJavaSystemProperties.keySet()) {
            args.append(" -D").append(propertyName).append("=").append(
                    additionalJavaSystemProperties.get(propertyName));
        }
        configuration.setProperty(GeneralPropertySet.JVMARGS, args.toString());
        AbstractInstalledLocalContainer instance = createContainer(container.getHomeDir(), container.getCargoId(),
                                                                   container.getId(), configuration);
        for (WebApplicationDescriptor webApplication : webApplications) {
            staticDeploy(webApplication, instance);
        }
        instance.start();
        container.setHomeDir(path.toString());
        container.setPort(serverPort);
        container.setRunningState(true);
        return container;
    }

    private boolean start(LocalContainer localContainer, Container entity) {
        boolean owned = false;
        if (entity.isRunning()) {
            return false;
        }
        try {
            if (lifeCycleOperationLock.tryLock(30, TimeUnit.SECONDS)) {
                owned = true;
                if (entity.setRunningState(true)) {
                    localContainer.start();
                    return true;
                }
                return false;
            }
        } catch (InterruptedException e) {
            log.error("caught InterruptedException", e);
            Thread.currentThread().interrupt();
        } catch (RuntimeException e) {
            entity.setRunningState(false);
            throw e;
        } finally {
            if (owned) {
                lifeCycleOperationLock.unlock();
            }
        }
        return false;
    }

    private AbstractInstalledLocalContainer loadDefault(boolean addStartupOptions) throws NotInstalledException {
        Container container = containersRepository.loadDefault();
        return loadContainer(container, addStartupOptions);
    }

    @SuppressWarnings("unchecked")
    private AbstractInstalledLocalContainer loadContainer(Container installedContainer, boolean addStartupOptions) {
        String cargoId = installedContainer.getCargoId();
        boolean standalone = installedContainer.isStandalone();
        if (!standalone && installedContainers.containsKey(cargoId)) {
            return installedContainers.get(cargoId);
        }
        //container creation
        AbstractInstalledLocalContainer container = createContainer(installedContainer.getHomeDir(), cargoId,
                                                                    installedContainer.getId(), null);
        if (standalone) {
            return container;
        }
        AbstractInstalledLocalContainer existing = installedContainers.putIfAbsent(cargoId, container);
        return existing == null ? container : existing;
    }

    private AbstractInstalledLocalContainer createContainer(String homeDir, String cargoId, String containerId, Configuration configuration) {
        if (configuration == null) {
            File configurationDir = new File(homeDir);
            configuration = loadExistingConfiguration(cargoId, configurationDir);
        }
        String agentPath = ContainerUtils.extractAgentJar(System.getProperty("java.class.path"));
        StringBuilder jvmArgs = new StringBuilder("-javaagent:");
        jvmArgs.append(agentPath);
        jvmArgs.append(" -Djava.net.preferIPv4Stack=true -Dejisto.database.port=");
        jvmArgs.append(System.getProperty(DATABASE_PORT.getValue()));
        jvmArgs.append(" -Dejisto.http.port=").append(System.getProperty(HTTP_LISTEN_PORT.getValue()));
        jvmArgs.append(" -D").append(StringConstants.CLASS_DEBUG_PATH.getValue()).append("=").append(
                FilenameUtils.normalize(System.getProperty("java.io.tmpdir") + "/"));

        String existingConfiguration = configuration.getPropertyValue(GeneralPropertySet.JVMARGS);
        if (StringUtils.isNotBlank(existingConfiguration)) {
            jvmArgs.append(" ").append(existingConfiguration);
        }
        configuration.setProperty(GeneralPropertySet.JVMARGS, jvmArgs.append(" ").toString());
        DefaultContainerFactory containerFactory = new DefaultContainerFactory();
        AbstractInstalledLocalContainer container = (AbstractInstalledLocalContainer) containerFactory.createContainer(
                cargoId,
                ContainerType.INSTALLED,
                configuration);
        container.setHome(homeDir);
        container.setLogger(new ServerLogger(containerId));
        container.addExtraClasspath(agentPath);
        return container;
    }


    private Configuration loadExistingConfiguration(String containerId, File configurationDir) {
        log.debug("loading existing configuration for container " + containerId);
        return new DefaultConfigurationFactory().createConfiguration(containerId,
                                                                     ContainerType.INSTALLED,
                                                                     ConfigurationType.EXISTING,
                                                                     configurationDir.getAbsolutePath());
    }

    @SuppressWarnings("unchecked")
    private Deployable staticDeploy(WebApplicationDescriptor webApplicationDescriptor, LocalContainer container) {
        try {
            Deployable deployable = createDeployable(webApplicationDescriptor, container);
            replaceDeployable(deployable, container);
            return deployable;
        } catch (Exception e) {
            log.error("error during static deploy", e);
            return null;
        }
    }

//    private Deployable hotDeploy(WebApplicationDescriptor webApplicationDescriptor, LocalContainer container) {
//        try {
//            Deployable deployable = createDeployable(webApplicationDescriptor, container);
//            Deployer deployer = getDeployerFor(container);
//            URLDeployableMonitor monitor = new URLDeployableMonitor(
//                    new URL(guessWebApplicationUri(webApplicationDescriptor)));
//            if (isAlreadyDeployed(deployable, container)) {
//                deployer.undeploy(deployable, monitor);
//            }
//            deployer.deploy(deployable, monitor);
//            return deployable;
//        } catch (Exception ex) {
//            log.error("error during hot deploy", ex);
//            return null;
//        }
//    }

    private boolean undeploy(String containerId, String contextPath, Deployable deployable, LocalContainer container) {
        try {
            URLDeployableMonitor monitor = new URLDeployableMonitor(new URL(guessWebApplicationUri(contextPath)));
            getDeployerFor(container).undeploy(deployable, monitor);
            webApplicationRepository.unregisterWebApplication(containerId, contextPath);
            return true;
        } catch (Exception ex) {
            log.error("error during undeploy", ex);
            return false;
        }
    }

    private boolean stop(String containerId, String contextPath, Deployable deployable, LocalContainer container) {
        try {
            getDeployerFor(container).stop(deployable);
            webApplicationRepository.getRegisteredWebApplication(containerId, contextPath).setStatus(
                    WebApplication.Status.STOPPED);
            return true;
        } catch (Exception ex) {
            log.error("error during web application stop", ex);
            return false;
        }
    }

    private boolean start(String containerId, String contextPath, Deployable deployable, LocalContainer container) {
        try {
            getDeployerFor(container).deploy(deployable);
            webApplicationRepository.getRegisteredWebApplication(containerId, contextPath).setStatus(STARTED);
            return true;
        } catch (Exception ex) {
            log.error("error during web application start", ex);
            return false;
        }
    }

    private Deployer getDeployerFor(LocalContainer container) {
        DeployerFactory deployerFactory = new DefaultDeployerFactory();
        return deployerFactory.createDeployer(container);
    }

    private Deployable createDeployable(WebApplicationDescriptor webApplicationDescriptor, LocalContainer container) {
        DeployableFactory deployableFactory = new DefaultDeployableFactory();
        Deployable deployable = deployableFactory.createDeployable(container.getId(),
                                                                   webApplicationDescriptor.getDeployablePath(),
                                                                   DeployableType.WAR);
        ((WAR) deployable).setContext(webApplicationDescriptor.getContextPath());
        return deployable;
    }

//    private boolean isAlreadyDeployed(Deployable deployable, LocalContainer container) {
//        return findDeployable(deployable.getFile(), container.getConfiguration()) != null;
//    }

    private void replaceDeployable(Deployable replacement, LocalContainer container) {
        LocalConfiguration configuration = container.getConfiguration();
        Deployable old = findDeployable(replacement.getFile(), configuration);
        if (old != null) {
            configuration.getDeployables().remove(old);
        }
        configuration.addDeployable(replacement);
    }

    private Deployable findDeployable(String fileName, LocalConfiguration configuration) {
        return selectFirst(configuration.getDeployables(), having(on(Deployable.class).getFile(), equalTo(fileName)));
    }

    private Deployable getDeployableFromRepository(String containerId, String contextPath) {
        WebApplication<?> webApplication = webApplicationRepository.getRegisteredWebApplication(containerId,
                                                                                                contextPath);
        return (Deployable) webApplication.getContainerWebApplicationDescriptor();
    }

}
