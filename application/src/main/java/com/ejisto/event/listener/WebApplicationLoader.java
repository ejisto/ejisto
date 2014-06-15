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
import com.ejisto.event.ApplicationListener;
import com.ejisto.event.EventManager;
import com.ejisto.event.def.ApplicationError;
import com.ejisto.event.def.LoadWebApplication;
import com.ejisto.modules.cargo.NotInstalledException;
import com.ejisto.modules.dao.entities.WebApplicationDescriptor;
import com.ejisto.modules.dao.local.LocalWebApplicationDescriptorDao;
import com.ejisto.modules.repository.ClassPoolRepository;
import com.ejisto.util.IOUtils;
import javassist.ClassPool;
import javassist.LoaderClassPath;
import lombok.extern.log4j.Log4j;

import java.net.MalformedURLException;
import java.net.URLClassLoader;

@Log4j
public class WebApplicationLoader implements ApplicationListener<LoadWebApplication> {

    private final EventManager eventManager;
    private final LocalWebApplicationDescriptorDao webApplicationDescriptorDao;
    private final ContainerManager containerManager;

    public WebApplicationLoader(EventManager eventManager,
                                LocalWebApplicationDescriptorDao webApplicationDescriptorDao,
                                ContainerManager containerManager) {
        this.eventManager = eventManager;
        this.webApplicationDescriptorDao = webApplicationDescriptorDao;
        this.containerManager = containerManager;
    }

    @Override
    public void onApplicationEvent(LoadWebApplication event) {
        try {
            if (event.loadStored()) {
                loadExistingWebApplications();
            }
        } catch (Exception e) {
            eventManager.publishEvent(new ApplicationError(this, ApplicationError.Priority.HIGH, e));
        }
    }

    @Override
    public Class<LoadWebApplication> getTargetEventType() {
        return LoadWebApplication.class;
    }

    private void loadExistingWebApplications() {
        loadWebAppDescriptors();
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

    private void deployWebApp(WebApplicationDescriptor webApplicationDescriptor) throws NotInstalledException {
        if (webApplicationDescriptor == null) {
            return;
        }
        containerManager.deployToDefaultContainer(webApplicationDescriptor);
    }

    private void registerClassPool(WebApplicationDescriptor descriptor) throws MalformedURLException {
        ClassPool cp = ClassPoolRepository.getRegisteredClassPool(descriptor.getContextPath());
        cp.appendClassPath(new LoaderClassPath(Thread.currentThread().getContextClassLoader()));
        cp.appendClassPath(new LoaderClassPath(new URLClassLoader(IOUtils.toUrlArray(descriptor))));
    }

}
