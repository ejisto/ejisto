/*
 * Copyright 2010 Celestino Bellone
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions
 * and limitations under the License.
 */

package com.ejisto.event.listener;

import static ch.lambdaj.Lambda.forEach;

import java.util.List;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.eclipse.jetty.server.HandlerContainer;
import org.eclipse.jetty.webapp.WebAppContext;
import org.springframework.context.ApplicationListener;

import com.ejisto.core.classloading.EjistoClassLoader;
import com.ejisto.event.EventManager;
import com.ejisto.event.def.ApplicationError;
import com.ejisto.event.def.LoadWebApplication;
import com.ejisto.modules.controller.ApplicationInstallerWizardController;
import com.ejisto.modules.dao.MockedFieldsDao;
import com.ejisto.modules.dao.entities.MockedField;
import com.ejisto.modules.gui.Application;
import com.ejisto.util.WebApplicationDescriptor;

public class WebApplicationLoader implements ApplicationListener<LoadWebApplication> {

    private static final Logger logger = Logger.getLogger(WebApplicationLoader.class);
    @Resource
    private Application application;
    @Resource
    private EventManager eventManager;
    @Resource
    private HandlerContainer contexts;
    @Resource
    private MockedFieldsDao mockedFieldsDao;
    
    @Override
    public void onApplicationEvent(LoadWebApplication event) {
        ApplicationInstallerWizardController controller = new ApplicationInstallerWizardController(application);
        if(!controller.showWizard()) return;
        WebApplicationDescriptor webApplicationDescriptor = controller.getWebApplicationDescriptor();
        List<MockedField> fields = webApplicationDescriptor.getModifiedFields();
        forEach(fields).setContextPath(webApplicationDescriptor.getContextPath());
        mockedFieldsDao.insert(fields);
        deployWebApp(webApplicationDescriptor);
    }
    
    private void deployWebApp(WebApplicationDescriptor webApplicationDescriptor) {
        if (webApplicationDescriptor == null)
            return;

        WebAppContext context = new WebAppContext(contexts, webApplicationDescriptor.getInstallationPath(), webApplicationDescriptor.getContextPath());
        context.setResourceBase(webApplicationDescriptor.getInstallationPath());

        try {
            EjistoClassLoader classLoader = new EjistoClassLoader(webApplicationDescriptor.getInstallationPath(), mockedFieldsDao.loadContextPathFields(context.getContextPath()), context);
            context.setClassLoader(classLoader);
            context.setParentLoaderPriority(false);
            context.start();
        } catch (Exception e) {
            eventManager.publishEvent(new ApplicationError(this, ApplicationError.Priority.HIGH, e));
            logger.error(e.getMessage(), e);
        }
    }
}
