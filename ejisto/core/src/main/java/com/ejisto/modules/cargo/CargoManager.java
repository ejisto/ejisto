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

import com.ejisto.core.container.ContainerManager;
import com.ejisto.core.container.WebApplication;
import com.ejisto.event.EventManager;
import com.ejisto.event.def.ChangeServerStatus;
import com.ejisto.modules.cargo.logging.ServerLogger;
import com.ejisto.modules.cargo.util.ContainerInstaller;
import com.ejisto.modules.dao.entities.Container;
import com.ejisto.modules.dao.entities.WebApplicationDescriptor;
import com.ejisto.modules.repository.ContainersRepository;
import com.ejisto.modules.repository.SettingsRepository;
import com.ejisto.modules.repository.WebApplicationRepository;
import com.ejisto.util.ContainerUtils;
import lombok.extern.log4j.Log4j;
import org.codehaus.cargo.container.ContainerType;
import org.codehaus.cargo.container.LocalContainer;
import org.codehaus.cargo.container.configuration.Configuration;
import org.codehaus.cargo.container.configuration.ConfigurationType;
import org.codehaus.cargo.container.configuration.LocalConfiguration;
import org.codehaus.cargo.container.deployable.Deployable;
import org.codehaus.cargo.container.deployable.DeployableType;
import org.codehaus.cargo.container.deployer.Deployer;
import org.codehaus.cargo.container.deployer.URLDeployableMonitor;
import org.codehaus.cargo.container.property.GeneralPropertySet;
import org.codehaus.cargo.container.property.ServletPropertySet;
import org.codehaus.cargo.container.spi.AbstractInstalledLocalContainer;
import org.codehaus.cargo.generic.DefaultContainerFactory;
import org.codehaus.cargo.generic.configuration.DefaultConfigurationFactory;
import org.codehaus.cargo.generic.deployable.DefaultDeployableFactory;
import org.codehaus.cargo.generic.deployable.DeployableFactory;
import org.codehaus.cargo.generic.deployer.DefaultDeployerFactory;
import org.codehaus.cargo.generic.deployer.DeployerFactory;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
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

    private static final String DEFAULT = DEFAULT_CONTAINER_ID.getValue();
    private final AtomicBoolean serverStarted = new AtomicBoolean(false);
    @Resource private ContainersRepository containersRepository;
    @Resource private SettingsRepository settingsRepository;
    @Resource private WebApplicationRepository webApplicationRepository;
    @Resource private EventManager eventManager;

    private final ConcurrentMap<String, AbstractInstalledLocalContainer> installedContainers = new ConcurrentHashMap<String, AbstractInstalledLocalContainer>();
    private final ReentrantLock lifeCycleOperationLock = new ReentrantLock();

    @Override
    public String downloadAndInstall(String urlToString, String folder) throws IOException {
        URL url = new URL(urlToString.trim());
        ContainerInstaller installer = new ContainerInstaller(url, folder);
        installer.install();
        containersRepository.registerDefaultContainer(DEFAULT, installer.getHome(),
                                                      settingsRepository.getSettingValue(
                                                              DEFAULT_CONTAINER_DESCRIPTION));
        return installer.getHome();
    }

    @Override
    public boolean isServerRunning() {
        return serverStarted.get();
    }

    @Override
    public boolean startDefault() throws NotInstalledException {
        return start(loadDefault(true));
    }

    @Override
    public boolean stopDefault() throws NotInstalledException {
        return !serverStarted.get() || stop(DEFAULT, loadDefault(false));
    }

    @Override
    public boolean start(Container container) throws NotInstalledException {
        return false;
    }

    @Override
    public boolean stop(Container container) throws NotInstalledException {
        return false;
    }

    private boolean start(LocalContainer localContainer) {
        boolean owned = false;
        try {
            if (lifeCycleOperationLock.tryLock(30, TimeUnit.SECONDS)) {
                owned = true;
                localContainer.start();
                serverStarted.set(true);
                return true;
            }
        } catch (InterruptedException e) {
            log.error("caught InterruptedException", e);
            Thread.currentThread().interrupt();
        } finally {
            if (owned) lifeCycleOperationLock.unlock();
        }
        return false;
    }

    @SuppressWarnings("unchecked")
    private boolean stop(String id, LocalContainer localContainer) {
        if (!serverStarted.get()) return true;
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
                serverStarted.set(false);
                return true;
            }
        } catch (InterruptedException e) {
            log.error("caught InterruptedException", e);
            Thread.currentThread().interrupt();
        } finally {
            if (owned) lifeCycleOperationLock.unlock();
        }
        return false;
    }

    private AbstractInstalledLocalContainer loadDefault(boolean addStartupOptions) throws NotInstalledException {
        Container container = containersRepository.loadDefault();
        return loadContainer(container, addStartupOptions);
    }

    @SuppressWarnings("unchecked")
    private AbstractInstalledLocalContainer loadContainer(Container installedContainer, boolean addStartupOptions) {
        String containerId = installedContainer.getCargoId();
        if (installedContainers.containsKey(containerId)) return installedContainers.get(containerId);
        //container creation
        File configurationDir = new File(installedContainer.getHomeDir());
        Configuration configuration;
        configuration = loadExistingConfiguration(containerId, configurationDir);
        String agentPath = ContainerUtils.extractAgentJar(System.getProperty("java.class.path"));
        StringBuilder jvmArgs = new StringBuilder("-javaagent:");
        jvmArgs.append(agentPath);
        jvmArgs.append(" -Djava.net.preferIPv4Stack=true -Dejisto.database.port=");
        jvmArgs.append(System.getProperty(DATABASE_PORT.getValue()));

        //if (addStartupOptions) jvmArgs.append(" -Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=5005");
        String existingConfiguration = configuration.getPropertyValue(GeneralPropertySet.JVMARGS);
        if (StringUtils.hasText(existingConfiguration)) jvmArgs.append(" ").append(existingConfiguration);
        configuration.setProperty(GeneralPropertySet.JVMARGS, jvmArgs.append(" ").toString());
        DefaultContainerFactory containerFactory = new DefaultContainerFactory();
        AbstractInstalledLocalContainer container = (AbstractInstalledLocalContainer) containerFactory.createContainer(
                containerId,
                ContainerType.INSTALLED,
                configuration);
        container.setHome(installedContainer.getHomeDir());
        container.setLogger(new ServerLogger());
        container.addExtraClasspath(agentPath);
        AbstractInstalledLocalContainer existing = installedContainers.putIfAbsent(containerId, container);
        return existing == null ? container : existing;
    }

    @Override
    public boolean deployToDefaultContainer(WebApplicationDescriptor webApplicationDescriptor) throws NotInstalledException {
        return deploy(webApplicationDescriptor, loadDefault(false), DEFAULT);
    }

    @Override
    public boolean deploy(WebApplicationDescriptor webApplicationDescriptor, Container container) throws NotInstalledException {
        return false;
    }

    @Override
    public boolean undeploy(String containerId, String contextPath) throws NotInstalledException {
        return false;
    }

    @Override
    public boolean undeployFromDefaultContainer(String contextPath) throws NotInstalledException {
        Deployable deployable = (Deployable) webApplicationRepository.getRegisteredWebApplication(DEFAULT,
                                                                                                  contextPath).getContainerWebApplicationDescriptor();
        undeploy(DEFAULT, contextPath, deployable, loadDefault(false));
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
        return start(DEFAULT, contextPath, getDeployableFromRepository(DEFAULT, contextPath), loadDefault(false));
    }

    @Override
    public boolean stopWebApplicationOnDefaultServer(String contextPath) throws NotInstalledException {
        return stop(DEFAULT, contextPath, getDeployableFromRepository(DEFAULT, contextPath), loadDefault(false));
    }

    @Override
    public String getDefaultHome() throws NotInstalledException {
        return getHome(containersRepository.loadDefault());
    }

    @Override
    public String getHome(Container container) {
        return loadContainer(container, false).getHome();
    }

    private Configuration loadExistingConfiguration(String containerId, File configurationDir) {
        log.debug("loading existing configuration for container " + containerId);
        Configuration configuration = new DefaultConfigurationFactory().createConfiguration(containerId,
                                                                                            ContainerType.INSTALLED,
                                                                                            ConfigurationType.EXISTING,
                                                                                            configurationDir.getAbsolutePath());
        configuration.setProperty(ServletPropertySet.PORT, settingsRepository.getSettingValue(DEFAULT_SERVER_PORT));
        return configuration;
    }

    private boolean deploy(WebApplicationDescriptor descriptor, LocalContainer container, String containerId) {
        boolean started = serverStarted.get();
        if (started) {
            eventManager.publishEventAndWait(new ChangeServerStatus(this, ChangeServerStatus.Command.SHUTDOWN));
        }
        //Deployable deployable = serverStarted.get() ? hotDeploy(descriptor, container) : staticDeploy(descriptor, container);
        Deployable deployable = staticDeploy(descriptor, container);
        if (deployable == null) return false;
        webApplicationRepository.registerWebApplication(containerId,
                                                        new CargoWebApplication(descriptor.getContextPath(),
                                                                                containerId, deployable));
        if (started) {
            eventManager.publishEventAndWait(new ChangeServerStatus(this, ChangeServerStatus.Command.STARTUP));
        }
        return true;
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
        return deployableFactory.createDeployable(container.getId(), webApplicationDescriptor.getDeployablePath(),
                                                  DeployableType.WAR);
    }

//    private boolean isAlreadyDeployed(Deployable deployable, LocalContainer container) {
//        return findDeployable(deployable.getFile(), container.getConfiguration()) != null;
//    }

    private void replaceDeployable(Deployable replacement, LocalContainer container) {
        LocalConfiguration configuration = container.getConfiguration();
        Deployable old = findDeployable(replacement.getFile(), configuration);
        if (old != null) configuration.getDeployables().remove(old);
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
