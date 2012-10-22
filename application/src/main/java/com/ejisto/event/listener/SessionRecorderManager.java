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

package com.ejisto.event.listener;

import com.ejisto.constants.StringConstants;
import com.ejisto.core.container.ContainerManager;
import com.ejisto.event.EventManager;
import com.ejisto.event.def.InstallContainer;
import com.ejisto.event.def.SessionRecorderStart;
import com.ejisto.modules.cargo.NotInstalledException;
import com.ejisto.modules.dao.entities.Container;
import com.ejisto.modules.dao.entities.WebApplicationDescriptor;
import com.ejisto.modules.dao.jdbc.WebApplicationDescriptorDao;
import com.ejisto.modules.executor.TaskManager;
import com.ejisto.modules.gui.Application;
import com.ejisto.modules.repository.ContainersRepository;
import com.ejisto.util.IOUtils;
import lombok.extern.log4j.Log4j;
import org.apache.commons.io.FilenameUtils;
import org.codehaus.cargo.module.DescriptorElement;
import org.codehaus.cargo.module.webapp.*;
import org.codehaus.cargo.module.webapp.elements.Filter;
import org.codehaus.cargo.module.webapp.elements.FilterMapping;
import org.jdom.JDOMException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.context.ApplicationListener;
import org.springframework.util.Assert;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.ejisto.constants.StringConstants.CONTEXT_PARAM_NAME;
import static com.ejisto.constants.StringConstants.SESSION_RECORDING_FILTER_NAME;
import static java.util.Arrays.asList;

/**
 * Created by IntelliJ IDEA.
 * User: celestino
 * Date: 9/17/12
 * Time: 8:16 AM
 */
@Log4j
public class SessionRecorderManager implements ApplicationListener<SessionRecorderStart>, DisposableBean {

    @Resource private ContainerManager containerManager;
    @Resource private EventManager eventManager;
    @Resource private Application application;
    @Resource private TaskManager taskManager;
    @Resource private WebApplicationDescriptorDao webApplicationDescriptorDao;
    @Resource private ContainersRepository containersRepository;
    private final List<String> startedContainers = new ArrayList<>();

    @Override
    public void onApplicationEvent(SessionRecorderStart event) {
        try {
            Map<String, String> systemProperties = new HashMap<>();
            systemProperties.put(StringConstants.SESSION_RECORDING_ACTIVE.getValue(), "true");
            WebApplicationDescriptor descriptor = createTempWebApplicationDescriptor(
                    webApplicationDescriptorDao.load(event.getWebApplicationContextPath()));
            Container container = containerManager.startStandaloneInstance(systemProperties, asList(descriptor));
            containersRepository.registerTemporaryContainer(container);
            int listeningPort = container.getPort();
            startedContainers.add(container.getId());
            log.debug("temporary instance listening on port " + listeningPort);
        } catch (NotInstalledException e) {
            log.error("server " + e.getId() + " is not installed.", e);
            eventManager.publishEvent(new InstallContainer(this, e.getId(), true));
        } catch (IOException | JDOMException e) {
            log.error("exception during temporary instance creation", e);
        }
    }

    @Override
    public void destroy() throws Exception {
        for (String containerId : startedContainers) {
            Container startedContainer = containersRepository.loadContainer(containerId);
            log.debug("stopping instance listening on port " + startedContainer.getPort());
            containerManager.stop(startedContainer);
        }
    }

    private WebApplicationDescriptor createTempWebApplicationDescriptor(WebApplicationDescriptor original) throws IOException, JDOMException {
        Path path = Files.createTempDirectory(original.getContextPath().replaceAll("/", "_"));
        IOUtils.copyDirContent(original.getDeployablePath(), path.toString());
        WebApplicationDescriptor temp = WebApplicationDescriptor.copyFrom(original);
        temp.setDeployablePath(path.toString());
        modifyWebXml(temp);
        return temp;
    }


    private void modifyWebXml(WebApplicationDescriptor descriptor) throws JDOMException, IOException {
        String path = FilenameUtils.normalizeNoEndSeparator(descriptor.getDeployablePath()) + File.separator +
                "WEB-INF" + File.separator + "web.xml";
        File webXml = new File(path);
        Assert.isTrue(webXml.exists());
        WebXml xml = WebXmlIo.parseWebXmlFromFile(webXml, null);
        DescriptorElement param = (DescriptorElement) WebXmlUtils.getContextParam(xml, CONTEXT_PARAM_NAME.getValue());
        if (param == null) {
            WebXmlUtils.addContextParam(xml, CONTEXT_PARAM_NAME.getValue(), descriptor.getContextPath());
        }
        buildFilter(xml, descriptor.getContextPath());
        WebXmlIo.writeDescriptor(xml, webXml);
    }

    @SuppressWarnings("unchecked")
    private void buildFilter(WebXml xml, String contextPath) {
        List<Object> elements = xml.getRootElement().getChildren();

        Filter filter = new Filter((WebXmlTag) xml.getDescriptorType().getTagByName(WebXmlType.FILTER),
                                   SESSION_RECORDING_FILTER_NAME.getValue(),
                                   "com.ejisto.modules.web.RequestPreprocessor");
        elements.add(0, filter);
        FilterMapping mapping = new FilterMapping(
                (WebXmlTag) xml.getDescriptorType().getTagByName(WebXmlType.FILTER_MAPPING));
        mapping.setFilterName(SESSION_RECORDING_FILTER_NAME.getValue());
        mapping.setUrlPattern("/*");
        elements.add(1, mapping);
    }


}
