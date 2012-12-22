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

import ch.lambdaj.Lambda;
import com.ejisto.event.EventManager;
import com.ejisto.event.def.ApplicationError;
import com.ejisto.event.def.CollectedDataReceived;
import com.ejisto.event.def.SessionRecorderStart;
import com.ejisto.modules.controller.DialogController;
import com.ejisto.modules.dao.entities.MockedField;
import com.ejisto.modules.dao.entities.WebApplicationDescriptor;
import com.ejisto.modules.dao.jdbc.WebApplicationDescriptorDao;
import com.ejisto.modules.gui.Application;
import com.ejisto.modules.gui.components.MockedFieldsEditor;
import com.ejisto.modules.gui.components.helper.FieldsEditorContext;
import com.ejisto.modules.recorder.CollectedData;
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
import org.jdesktop.swingx.action.AbstractActionExt;
import org.jdom.JDOMException;
import org.springframework.context.ApplicationListener;
import org.springframework.util.Assert;

import javax.annotation.Resource;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicReference;

import static com.ejisto.constants.StringConstants.*;
import static com.ejisto.modules.gui.components.EjistoDialog.DEFAULT_HEIGHT;
import static com.ejisto.modules.gui.components.EjistoDialog.DEFAULT_WIDTH;
import static com.ejisto.util.GuiUtils.*;
import static com.ejisto.util.IOUtils.*;

/**
 * Created by IntelliJ IDEA.
 * User: celestino
 * Date: 9/17/12
 * Time: 8:16 AM
 */
@Log4j
public class SessionRecorderManager implements ApplicationListener<SessionRecorderStart> {

    private static final ConcurrentMap<String, Set<MockedField>> RECORDED_FIELDS = new ConcurrentHashMap<>();
    private static final ConcurrentMap<String, Set<CollectedData>> RECORDED_DATA = new ConcurrentHashMap<>();
    @Resource private EventManager eventManager;
    @Resource private Application application;
    @Resource private WebApplicationDescriptorDao webApplicationDescriptorDao;
    @Resource private HTTPServer httpServer;
    @Resource private SettingsRepository settingsRepository;
    private final AtomicReference<DialogController> dialogController = new AtomicReference<>();


    @Override
    public void onApplicationEvent(SessionRecorderStart event) {
        WebApplicationDescriptor descriptor = webApplicationDescriptorDao.load(event.getWebApplicationContextPath());
        if (descriptor.getWarFile() == null) {
            File war = GuiUtils.selectFile(application, settingsRepository.getSettingValue(LAST_FILESELECTION_PATH),
                                           false, "war");
            if (war == null) {
                GuiUtils.showErrorMessage(application, getMessage("wizard.file.selected.default.text"));
                return;
            }
            descriptor.setWarFile(war);
        }
        startSessionRecording(descriptor);
    }

    public void startSessionRecording(WebApplicationDescriptor webApplicationDescriptor) {
        try {
            log.debug("start listening for collected data...");
            File outputDir = GuiUtils.selectDirectory(application, settingsRepository.getSettingValue(LAST_OUTPUT_PATH),
                                                      true);
            final WebApplicationDescriptor descriptor = createTempWebApplicationDescriptor(webApplicationDescriptor);
            zipDirectory(new File(descriptor.getDeployablePath()),
                         FilenameUtils.normalize(outputDir.getAbsolutePath() + descriptor.getContextPath() + ".war"));
            if (!httpServer.createContext(descriptor.getContextPath(), new DataCollectorHandler(eventManager))) {
                log.warn("attempt to create httpHandler failed. A Handler has already been defined.");
            }
            log.debug("done.");
            final MockedFieldsEditor editor = new MockedFieldsEditor(FieldsEditorContext.ADD_FIELD, new ActionMap());
            DialogController controller = DialogController.Builder.newInstance()
                    .resizable(true)
                    .withActions(new AbstractActionExt(getMessage("session.record.stop"), getIcon(
                            "session.record.stop.icon")) {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            stopRecording(descriptor.getContextPath());
                        }
                    })
                    .withContent(editor)
                    .withHeader(getMessage("session.record.title"), getMessage("session.record.description"))
                    .withIconKey("session.record.icon")
                    .withParentFrame(application)
                    .build();
            if (dialogController.compareAndSet(null, controller)) {
                registerEventListener(CollectedDataReceived.class,
                                      new ApplicationListener<CollectedDataReceived>() {
                                          @Override
                                          public void onApplicationEvent(CollectedDataReceived event) {
                                              try {
                                                  CollectedData collectedData = event.getData();
                                                  String contextPath = collectedData.getContextPath();
                                                  Set<MockedField> fields = putIfAbsent(contextPath, RECORDED_FIELDS);
                                                  Set<CollectedData> data = putIfAbsent(contextPath, RECORDED_DATA);
                                                  fields.addAll(flattenAttributes(collectedData.getRequestAttributes()));
                                                  fields.addAll(flattenAttributes(collectedData.getSessionAttributes()));
                                                  replace(contextPath, RECORDED_FIELDS, fields);
                                                  data.add(collectedData);
                                                  replace(contextPath, RECORDED_DATA, data);
                                                  editor.setFields(new ArrayList<>(RECORDED_FIELDS.get(contextPath)));
                                              } catch (Exception e) {
                                                  SessionRecorderManager.log.error("got exception while collecting fields", e);
                                              }
                                          }
                                      });
                controller.show(true, new Dimension(DEFAULT_WIDTH, DEFAULT_HEIGHT));
            }
        } catch (IOException | JDOMException e) {
            eventManager.publishEvent(new ApplicationError(this, ApplicationError.Priority.HIGH, e));
        }
    }

    private List<MockedField> flattenAttributes(Map<String, List<MockedField>> requestAttributes) {
        return Lambda.flatten(requestAttributes);
    }

    private <K, V> boolean replace(K key, ConcurrentMap<K, Set<V>> container, Set<V> newValue) {
        int counter = 10;
        while(counter-- > 0){
            Set<V> currentValue = container.get(key);
            Set<V> copy = new HashSet<>(currentValue);
            copy.addAll(newValue);
            if(container.replace(key, currentValue, copy)) {
                return true;
            }
        }
        return false;
    }

    private <K, V> Set<V> putIfAbsent(K key, ConcurrentMap<K, Set<V>> container) {
        Set<V> values = container.get(key);
        if (values == null) {
            container.putIfAbsent(key, new HashSet<V>());
            values = container.get(key);
        }
        return new HashSet<>(values);
    }

    private void stopRecording(String contextPath) {
        DialogController controller = dialogController.get();
        controller.hide();
        dialogController.compareAndSet(controller, null);
        httpServer.removeContext(contextPath);
        //do some stuff with collected data
    }

    private WebApplicationDescriptor createTempWebApplicationDescriptor(WebApplicationDescriptor original) throws IOException, JDOMException {
        Path path = Files.createTempDirectory(original.getContextPath().replaceAll("/", "_"));
        //copyDirContentExcludingMatchingFiles(new File(original.getDeployablePath()), new File(path.toString()), new String[] {COPIED_FILES_PREFIX});
        unzipFile(original.getWarFile(), path.toString());
        File targetDir = new File(FilenameUtils.normalize(path.toString() + "/WEB-INF/lib/"));
        copyFile(System.getProperty("ejisto.agent.jar.path"), targetDir);
        copyEjistoLibs(new String[]{"jackson", "commons-lang"}, targetDir);
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
