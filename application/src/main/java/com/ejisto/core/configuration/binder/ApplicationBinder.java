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

package com.ejisto.core.configuration.binder;

import com.ejisto.core.launcher.ApplicationController;
import com.ejisto.event.ApplicationListener;
import com.ejisto.event.listener.*;
import com.ejisto.modules.gui.Application;
import com.ejisto.services.Service;
import com.ejisto.services.shutdown.ContainerShutdown;
import com.ejisto.services.shutdown.DatabaseMaintenance;
import com.ejisto.services.shutdown.FolderCleaner;
import com.ejisto.services.startup.*;
import se.jbee.inject.bind.BinderModule;

/**
 * Created by IntelliJ IDEA.
 * User: celestino
 * Date: 3/28/13
 * Time: 5:45 PM
 */
public class ApplicationBinder extends BinderModule {

    @Override
    protected void declare() {
        construct(ApplicationController.class);
        construct(Application.class);
        construct(ServerController.class);
        construct(WebApplicationLoader.class);
        construct(ErrorListener.class);
        construct(FieldsUpdateListener.class);
        construct(ContainerInstaller.class);
        construct(MockedFieldOperationListener.class);
        construct(DialogManager.class);
        construct(WebApplicationScanner.class);
        construct(TaskProgressNotifier.class);
        construct(SessionRecorderManager.class);
        multibind(Service.class).to(ConstraintsVerifier.class);
        multibind(Service.class).to(ContainerShutdown.class);
        multibind(Service.class).to(ResourcesInitializer.class);
        multibind(Service.class).to(TaskInitializer.class);
        multibind(Service.class).to(ApplicationStartup.class);
        multibind(Service.class).to(DatabaseMaintenance.class);
        multibind(Service.class).to(FolderCleaner.class);
        multibind(Service.class).to(EventListenerRegistrar.class);
        multibind(ApplicationListener.class).to(CollectedDataNotifier.class);
        multibind(ApplicationListener.class).to(ContainerInstaller.class);
        multibind(ApplicationListener.class).to(DialogManager.class);
        multibind(ApplicationListener.class).to(ErrorListener.class);
        multibind(ApplicationListener.class).to(FieldsUpdateListener.class);
        multibind(ApplicationListener.class).to(MockedFieldOperationListener.class);
        multibind(ApplicationListener.class).to(ServerController.class);
        multibind(ApplicationListener.class).to(SessionRecorderManager.class);
        multibind(ApplicationListener.class).to(TaskProgressNotifier.class);
        multibind(ApplicationListener.class).to(WebApplicationLoader.class);
        multibind(ApplicationListener.class).to(WebApplicationScanner.class);
    }
}
