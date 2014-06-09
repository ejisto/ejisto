/*
 * Ejisto, a powerful developer assistant
 *
 * Copyright (C) 2010-2014 Celestino Bellone
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
import com.ejisto.event.def.ApplicationDeployed;
import com.ejisto.event.def.ApplicationInstallFinalization;
import com.ejisto.event.def.StatusBarMessage;
import com.ejisto.modules.dao.entities.Container;
import com.ejisto.modules.dao.entities.WebApplicationDescriptor;
import com.ejisto.modules.repository.ClassPoolRepository;
import com.ejisto.util.IOUtils;
import javassist.ClassPool;
import javassist.LoaderClassPath;

import java.net.MalformedURLException;
import java.net.URLClassLoader;

import static com.ejisto.util.GuiUtils.getMessage;

/**
 * Created by IntelliJ IDEA.
 * User: celestino
 * Date: 5/15/14
 * Time: 6:08 PM
 */
public class ApplicationInstallFinalizer implements ApplicationListener<ApplicationInstallFinalization> {

    private final ContainerManager containerManager;
    private final EventManager eventManager;

    public ApplicationInstallFinalizer(ContainerManager containerManager, EventManager eventManager) {
        this.containerManager = containerManager;
        this.eventManager = eventManager;
    }

    @Override
    public void onApplicationEvent(ApplicationInstallFinalization event) {
        final WebApplicationDescriptor descriptor = event.getDescriptor();
        final Container container = event.getTargetContainer();
        containerManager.deploy(descriptor, container);
        initClassPool(descriptor);
        eventManager.publishEvent(new ApplicationDeployed(this, descriptor.getContextPath(), container.getId()));
        eventManager.publishEvent(new StatusBarMessage(this, getMessage("statusbar.installation.successful.message",
                                                                        descriptor.getContextPath()), false));
    }

    private void initClassPool(WebApplicationDescriptor descriptor) {
        try {
            ClassPool classPool = ClassPoolRepository.getRegisteredClassPool(descriptor.getContextPath());
            classPool.appendClassPath(new LoaderClassPath(new URLClassLoader(IOUtils.toUrlArray(descriptor))));
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException(e);
        }
    }

    @Override
    public Class<ApplicationInstallFinalization> getTargetEventType() {
        return ApplicationInstallFinalization.class;
    }
}
