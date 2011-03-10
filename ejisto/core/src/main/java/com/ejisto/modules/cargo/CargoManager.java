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

package com.ejisto.modules.cargo;

import com.ejisto.core.container.ContainerManager;
import com.ejisto.modules.cargo.logging.ServerLogger;
import com.ejisto.modules.cargo.util.ContainerInstaller;
import com.ejisto.modules.dao.entities.Container;
import com.ejisto.modules.dao.entities.WebApplicationDescriptor;
import com.ejisto.modules.repository.ContainersRepository;
import com.ejisto.util.ContainerUtils;
import org.apache.log4j.Logger;
import org.codehaus.cargo.container.ContainerType;
import org.codehaus.cargo.container.LocalContainer;
import org.codehaus.cargo.container.configuration.Configuration;
import org.codehaus.cargo.container.configuration.ConfigurationType;
import org.codehaus.cargo.container.deployable.Deployable;
import org.codehaus.cargo.container.deployable.DeployableType;
import org.codehaus.cargo.container.deployer.Deployer;
import org.codehaus.cargo.container.deployer.URLDeployableMonitor;
import org.codehaus.cargo.container.spi.AbstractInstalledLocalContainer;
import org.codehaus.cargo.generic.DefaultContainerFactory;
import org.codehaus.cargo.generic.configuration.ConfigurationFactory;
import org.codehaus.cargo.generic.configuration.DefaultConfigurationFactory;
import org.codehaus.cargo.generic.deployable.DefaultDeployableFactory;
import org.codehaus.cargo.generic.deployable.DeployableFactory;
import org.codehaus.cargo.generic.deployer.DefaultDeployerFactory;
import org.codehaus.cargo.generic.deployer.DeployerFactory;

import javax.annotation.Resource;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import static com.ejisto.util.IOUtils.guessWebApplicationUri;

/**
 * Created by IntelliJ IDEA.
 * User: celestino
 * Date: 2/18/11
 * Time: 7:23 PM
 */
public class CargoManager implements ContainerManager {

    private static final Logger logger = Logger.getLogger(CargoManager.class);
    private static final String DEFAULT = "tomcat7x";
    private boolean serverStarted = false;
    @Resource private ContainersRepository containersRepository;
    private HashMap<String, AbstractInstalledLocalContainer> installedContainers = new HashMap<String, AbstractInstalledLocalContainer>();

    @Override
    public String downloadAndInstall(String urlToString, String folder) throws IOException {
        final URL url = new URL(urlToString.trim());
        ContainerInstaller installer = new ContainerInstaller(url, folder);
        installer.install();
        containersRepository.registerDefaultContainer(DEFAULT, installer.getHome(), DEFAULT);
        return installer.getHome();
    }

    @Override
    public boolean isServerRunning() {
        return serverStarted;
    }

    @Override
    public boolean startDefault() throws NotInstalledException {
        return start(loadDefault());
    }

    @Override
    public boolean stopDefault() throws NotInstalledException {
        return stop(loadDefault());
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
        localContainer.start();
        serverStarted = true;
        return true;
    }

    private boolean stop(LocalContainer localContainer) {
        localContainer.stop();
        serverStarted = true;
        return true;
    }

    private AbstractInstalledLocalContainer loadDefault() throws NotInstalledException {
        Container container = containersRepository.loadDefault();
        return loadContainer(container.getCargoId(), container.getHomeDir());
    }

    @SuppressWarnings("unchecked")
    private synchronized AbstractInstalledLocalContainer loadContainer(String containerId, String serverHome) {
        if (installedContainers.containsKey(containerId)) return installedContainers.get(containerId);
        ConfigurationFactory configurationFactory = new DefaultConfigurationFactory();
        Configuration configuration = configurationFactory.createConfiguration(containerId, ContainerType.INSTALLED, ConfigurationType.EXISTING,
                                                                               serverHome);
        DefaultContainerFactory containerFactory = new DefaultContainerFactory();
        AbstractInstalledLocalContainer container = (AbstractInstalledLocalContainer) containerFactory.createContainer(containerId,
                                                                                                                       ContainerType.INSTALLED,
                                                                                                                       configuration);
        container.setHome(serverHome);
        container.setLogger(new ServerLogger());
        String agentPath = ContainerUtils.extractAgentJar(System.getProperty("java.class.path"));
        container.addExtraClasspath(agentPath);
        Map<String, String> systemProperties = container.getSystemProperties();
        if (systemProperties == null) systemProperties = new HashMap<String, String>();
        systemProperties.put("cargo.jvmargs", "-javaagent:" + agentPath + " -Xmx512m -Xms128m -XX:MaxPermSize=128m ");
        container.setSystemProperties(systemProperties);
        installedContainers.put(containerId, container);
        return container;
    }

    @Override
    public boolean deployToDefaultContainer(WebApplicationDescriptor webApplicationDescriptor) throws NotInstalledException {
        return deploy(webApplicationDescriptor, loadDefault());
    }

    @Override
    public boolean deploy(WebApplicationDescriptor webApplicationDescriptor, Container container) throws NotInstalledException {
        return false;
    }

    @Override
    public String getDefaultHome() throws NotInstalledException {
        return getHome(containersRepository.loadDefault());
    }

    @Override
    public String getHome(Container container) {
        return loadContainer(container.getCargoId(), container.getHomeDir()).getHome();
    }

    private boolean deploy(WebApplicationDescriptor webApplicationDescriptor, LocalContainer container) {
        if (serverStarted) return hotDeploy(webApplicationDescriptor, container);
        return staticDeploy(webApplicationDescriptor, container);
    }

    private boolean staticDeploy(WebApplicationDescriptor webApplicationDescriptor, LocalContainer container) {
        try {
            Deployable deployable = createDeployable(webApplicationDescriptor, container);
            container.getConfiguration().addDeployable(deployable);
            return true;
        } catch (Exception e) {
            logger.error("error during static deploy", e);
            return false;
        }
    }

    private boolean hotDeploy(WebApplicationDescriptor webApplicationDescriptor, LocalContainer container) {
        try {
            Deployable deployable = createDeployable(webApplicationDescriptor, container);
            DeployerFactory deployerFactory = new DefaultDeployerFactory();
            Deployer deployer = deployerFactory.createDeployer(container);
            URLDeployableMonitor monitor = new URLDeployableMonitor(new URL(guessWebApplicationUri(webApplicationDescriptor)));
            if (isAlreadyDeployed(deployable, container)) {
                deployer.undeploy(deployable, monitor);
                deployer.deploy(deployable, monitor);
            } else {
                deployer.deploy(deployable, monitor);
            }
            return true;
        } catch (Exception ex) {
            logger.error("error during hot deploy", ex);
            return false;
        }
    }

    private Deployable createDeployable(WebApplicationDescriptor webApplicationDescriptor, LocalContainer container) {
        DeployableFactory deployableFactory = new DefaultDeployableFactory();
        return deployableFactory.createDeployable(container.getId(), webApplicationDescriptor.getDeployablePath(), DeployableType.WAR);
    }

    @SuppressWarnings("unchecked")
    private boolean isAlreadyDeployed(Deployable deployable, LocalContainer container) {
        return container.getConfiguration().getDeployables().contains(deployable);
    }

}
