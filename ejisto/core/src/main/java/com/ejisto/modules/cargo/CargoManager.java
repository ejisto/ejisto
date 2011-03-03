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

import com.ejisto.modules.dao.entities.WebApplicationDescriptor;
import org.apache.log4j.Logger;
import org.codehaus.cargo.container.ContainerType;
import org.codehaus.cargo.container.LocalContainer;
import org.codehaus.cargo.container.configuration.Configuration;
import org.codehaus.cargo.container.configuration.ConfigurationType;
import org.codehaus.cargo.container.deployable.Deployable;
import org.codehaus.cargo.container.deployable.DeployableType;
import org.codehaus.cargo.container.deployer.Deployer;
import org.codehaus.cargo.container.deployer.URLDeployableMonitor;
import org.codehaus.cargo.container.installer.Installer;
import org.codehaus.cargo.container.installer.ZipURLInstaller;
import org.codehaus.cargo.generic.DefaultContainerFactory;
import org.codehaus.cargo.generic.configuration.ConfigurationFactory;
import org.codehaus.cargo.generic.configuration.DefaultConfigurationFactory;
import org.codehaus.cargo.generic.deployable.DefaultDeployableFactory;
import org.codehaus.cargo.generic.deployable.DeployableFactory;
import org.codehaus.cargo.generic.deployer.DefaultDeployerFactory;
import org.codehaus.cargo.generic.deployer.DeployerFactory;

import java.io.IOException;
import java.net.URL;

import static com.ejisto.util.IOUtils.guessWebApplicationUri;

/**
 * Created by IntelliJ IDEA.
 * User: celestino
 * Date: 2/18/11
 * Time: 7:23 PM
 */
public class CargoManager {

    private static final Logger logger = Logger.getLogger(CargoManager.class);
    private boolean serverStarted = false;

    public String downloadAndInstall(String url, String folder) throws IOException {
        Installer installer = new ZipURLInstaller(new URL(url),folder);
        installer.install();
        return installer.getHome();
    }

    public boolean start(LocalContainer localContainer) {
        localContainer.start();
        serverStarted = true;
        return true;
    }

    public LocalContainer loadContainer(String containerId, String serverHome) {
        ConfigurationFactory configurationFactory = new DefaultConfigurationFactory();
        Configuration configuration = configurationFactory.createConfiguration(containerId, ContainerType.INSTALLED, ConfigurationType.EXISTING, serverHome);
        DefaultContainerFactory containerFactory = new DefaultContainerFactory();
        LocalContainer container = (LocalContainer)containerFactory.createContainer(containerId, ContainerType.INSTALLED, configuration);
        return container;
    }

    public boolean deploy(WebApplicationDescriptor webApplicationDescriptor, LocalContainer container) {
        if(serverStarted) return hotDeploy(webApplicationDescriptor, container);
        return staticDeploy(webApplicationDescriptor, container);
    }

    public boolean staticDeploy(WebApplicationDescriptor webApplicationDescriptor, LocalContainer container) {
        try {
            Deployable deployable = createDeployable(webApplicationDescriptor, container);
            container.getConfiguration().addDeployable(deployable);
            return true;
        } catch (Exception e) {
            logger.error("error during static deploy", e);
            return false;
        }
    }

    public boolean hotDeploy(WebApplicationDescriptor webApplicationDescriptor, LocalContainer container) {
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

    public Deployable createDeployable(WebApplicationDescriptor webApplicationDescriptor, LocalContainer container) {
        DeployableFactory deployableFactory = new DefaultDeployableFactory();
        return deployableFactory.createDeployable(container.getId(), webApplicationDescriptor.getDeployablePath(), DeployableType.WAR);
    }

    @SuppressWarnings("unchecked")
    private boolean isAlreadyDeployed(Deployable deployable, LocalContainer container) {
        return container.getConfiguration().getDeployables().contains(deployable);
    }

}
