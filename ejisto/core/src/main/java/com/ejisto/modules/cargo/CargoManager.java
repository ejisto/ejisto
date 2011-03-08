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

import com.ejisto.modules.cargo.logging.ServerLogger;
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
import org.codehaus.cargo.container.installer.ZipURLInstaller;
import org.codehaus.cargo.container.spi.AbstractInstalledLocalContainer;
import org.codehaus.cargo.generic.DefaultContainerFactory;
import org.codehaus.cargo.generic.configuration.ConfigurationFactory;
import org.codehaus.cargo.generic.configuration.DefaultConfigurationFactory;
import org.codehaus.cargo.generic.deployable.DefaultDeployableFactory;
import org.codehaus.cargo.generic.deployable.DeployableFactory;
import org.codehaus.cargo.generic.deployer.DefaultDeployerFactory;
import org.codehaus.cargo.generic.deployer.DeployerFactory;

import javax.annotation.Resource;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.HashMap;
import java.util.Map;

import static com.ejisto.util.IOUtils.guessWebApplicationUri;

/**
 * Created by IntelliJ IDEA.
 * User: celestino
 * Date: 2/18/11
 * Time: 7:23 PM
 */
public class CargoManager {

    private static final Logger logger = Logger.getLogger(CargoManager.class);
    private static final String DEFAULT = "tomcat7x";
    private boolean serverStarted = false;
    @Resource
    private ContainersRepository containersRepository;

    public String downloadAndInstall(String urlToString, String folder) throws IOException {
        final URL url = new URL(urlToString.trim());
        ZipURLInstaller installer = new ZipURLInstaller(url, folder) {
            @Override
            protected void download() {
                try {
                    URLConnection connection = url.openConnection();
//                    connection.setDoInput(true);
//                    connection.setDoOutput(false);
                    connection.connect();
                    int total = connection.getContentLength();
                    BufferedInputStream bis = new BufferedInputStream(connection.getInputStream());
                    FileOutputStream out = new FileOutputStream(new File(getDestinationDir(), getSourceFileName()));
                    FileChannel ch = out.getChannel();
                    byte[] buffer = new byte[512000];
                    int readed;
                    int totalReaded = 0;
                    while ((readed = bis.read(buffer)) != -1) {
                        totalReaded += readed;
                        logger.debug("readed " + totalReaded + " of " + total);
                        ch.write(ByteBuffer.wrap(buffer, 0, readed));
                    }
                    ch.close();
                    out.close();

                } catch (IOException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }
            }
        };

        installer.install();
        containersRepository.registerDefaultContainer(DEFAULT, installer.getHome(), DEFAULT);
        return installer.getHome();
    }

    public boolean isServerRunning() {
        return serverStarted;
    }

    public boolean startDefault() throws NotInstalledException {
        return start(loadDefault());
    }

    public boolean stopDefault() throws NotInstalledException {
        return stop(loadDefault());
    }

    /**
     * Start a local container.
     * Currently this method has private access
     *
     * @param localContainer the container to start
     * @return <code>true</true> if started
     */
    private boolean start(LocalContainer localContainer) {
        localContainer.start();
        serverStarted = true;
        return true;
    }

    /**
     * Stop a local container
     * Currently this method has private access
     *
     * @param localContainer the container to stop
     * @return <code>true</true> if stopped
     */
    private boolean stop(LocalContainer localContainer) {
        localContainer.stop();
        serverStarted = true;
        return true;
    }

    public AbstractInstalledLocalContainer loadDefault() throws NotInstalledException {
        Container container = containersRepository.loadDefault();
        return loadContainer(container.getCargoId(), container.getHomeDir());
    }

    public AbstractInstalledLocalContainer loadContainer(String containerId, String serverHome) {
        ConfigurationFactory configurationFactory = new DefaultConfigurationFactory();
        Configuration configuration = configurationFactory.createConfiguration(containerId, ContainerType.INSTALLED,
                                                                               ConfigurationType.EXISTING, serverHome);
        DefaultContainerFactory containerFactory = new DefaultContainerFactory();
        AbstractInstalledLocalContainer container = (AbstractInstalledLocalContainer) containerFactory.createContainer(
                containerId, ContainerType.INSTALLED, configuration);
        container.setHome(serverHome);
        container.setLogger(new ServerLogger());
        String agentPath = ContainerUtils.extractAgentJar(System.getProperty("java.class.path"));
        container.addExtraClasspath(agentPath);
        Map<String, String> systemProperties = container.getSystemProperties();
        if (systemProperties == null) systemProperties = new HashMap<String, String>();
        systemProperties.put("cargo.jvmargs", "-javaagent:" + agentPath + " -Xmx512m -Xms128m -XX:MaxPermSize=128m ");
        container.setSystemProperties(systemProperties);
        return container;
    }

    public boolean deployToDefaultContainer(WebApplicationDescriptor webApplicationDescriptor) throws NotInstalledException {
        return deploy(webApplicationDescriptor, loadDefault());
    }

    public boolean deploy(WebApplicationDescriptor webApplicationDescriptor, LocalContainer container) {
        if (serverStarted) return hotDeploy(webApplicationDescriptor, container);
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
            URLDeployableMonitor monitor = new URLDeployableMonitor(
                    new URL(guessWebApplicationUri(webApplicationDescriptor)));
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
        return deployableFactory.createDeployable(container.getId(), webApplicationDescriptor.getDeployablePath(),
                                                  DeployableType.WAR);
    }

    @SuppressWarnings("unchecked")
    private boolean isAlreadyDeployed(Deployable deployable, LocalContainer container) {
        return container.getConfiguration().getDeployables().contains(deployable);
    }

}
