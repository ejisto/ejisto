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

import com.ejisto.core.container.ContainerManager;
import com.ejisto.event.EventManager;
import com.ejisto.event.def.SessionRecorderStart;
import com.ejisto.modules.dao.entities.WebApplicationDescriptor;
import com.ejisto.modules.dao.jdbc.WebApplicationDescriptorDao;
import com.ejisto.modules.gui.Application;
import com.ejisto.modules.repository.SettingsRepository;
import com.ejisto.modules.web.HTTPServer;
import com.ejisto.modules.web.handler.DataCollectorHandler;
import com.ejisto.util.GuiUtils;
import lombok.extern.log4j.Log4j;
import org.apache.commons.io.FilenameUtils;
import org.codehaus.cargo.module.DescriptorElement;
import org.codehaus.cargo.module.webapp.*;
import org.codehaus.cargo.module.webapp.elements.Filter;
import org.codehaus.cargo.module.webapp.elements.FilterMapping;
import org.jdom.JDOMException;
import org.springframework.context.ApplicationListener;
import org.springframework.util.Assert;

import javax.annotation.Resource;
import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static com.ejisto.constants.StringConstants.*;
import static com.ejisto.util.IOUtils.*;

/**
 * Created by IntelliJ IDEA.
 * User: celestino
 * Date: 9/17/12
 * Time: 8:16 AM
 */
@Log4j
public class SessionRecorderManager implements ApplicationListener<SessionRecorderStart> {

    @Resource private ContainerManager containerManager;
    @Resource private EventManager eventManager;
    @Resource private Application application;
    @Resource private WebApplicationDescriptorDao webApplicationDescriptorDao;
    @Resource private HTTPServer httpServer;
    @Resource private SettingsRepository settingsRepository;

    @Override
    public void onApplicationEvent(SessionRecorderStart event) {
        try {
            log.debug("start listening for collected data...");
            File outputDir = GuiUtils.selectDirectory(application, settingsRepository.getSettingValue(LAST_OUTPUT_PATH),
                                                      true);
            WebApplicationDescriptor descriptor = createTempWebApplicationDescriptor(
                    webApplicationDescriptorDao.load(event.getWebApplicationContextPath()));
            zipDirectory(new File(descriptor.getDeployablePath()),
                         FilenameUtils.normalize(outputDir.getAbsolutePath() + descriptor.getContextPath() + ".war"));
            if (!httpServer.createContext(event.getWebApplicationContextPath(),
                                          new DataCollectorHandler(eventManager))) {
                log.info("attempt to create httpHandler failed. A Handler has already been defined.");
            }
            log.debug("done.");
            JOptionPane.showMessageDialog(application, "start server");
        } catch (IOException | JDOMException e) {
            log.error("exception during temporary instance creation", e);
        }
    }

    private WebApplicationDescriptor createTempWebApplicationDescriptor(WebApplicationDescriptor original) throws IOException, JDOMException {
        Path path = Files.createTempDirectory(original.getContextPath().replaceAll("/", "_"));
        //copyDirContentExcludingMatchingFiles(new File(original.getDeployablePath()), new File(path.toString()), new String[] {COPIED_FILES_PREFIX});
        unzipFile(new File("/home/celestino/progetti/petclinic/target/petclinic.war"), path.toString());
        File destDir = new File(FilenameUtils.normalize(path.toString() + "/WEB-INF/lib/"));
        copyFile(System.getProperty("ejisto.agent.jar.path"), destDir);
        copyEjistoLibs(new String[]{"jackson", "commons-lang"}, destDir);
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
        DescriptorElement param = (DescriptorElement) WebXmlUtils.getContextParam(xml, TARGET_CONTEXT_PATH.getValue());
        if (param == null) {
            WebXmlUtils.addContextParam(xml, TARGET_CONTEXT_PATH.getValue(), descriptor.getContextPath());
        }
        buildFilter(xml);
        WebXmlUtils.addContextParam(xml, HTTP_INTERFACE_ADDRESS.getValue(), getHttpInterfaceAddress());
        WebXmlUtils.addContextParam(xml, SESSION_RECORDING_ACTIVE.getValue(), Boolean.TRUE.toString());
        WebXmlIo.writeDescriptor(xml, webXml);
    }

    private void buildFilter(WebXml xml) {
        @SuppressWarnings("unchecked")
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
