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

import com.ejisto.core.classloading.SharedClassLoader;
import com.ejisto.core.classloading.javassist.PropertyManager;
import com.ejisto.core.container.ContainerManager;
import com.ejisto.event.ApplicationEventDispatcher;
import com.ejisto.event.EventManager;
import com.ejisto.modules.cargo.CargoManager;
import com.ejisto.modules.conf.SettingsManager;
import com.ejisto.modules.dao.db.EmbeddedDatabaseManager;
import com.ejisto.modules.executor.TaskManager;
import com.ejisto.modules.repository.*;
import com.ejisto.modules.web.HTTPServer;
import com.ejisto.modules.web.RemoteRequestHandler;
import com.ejisto.modules.web.handler.*;
import ognl.OgnlContext;
import se.jbee.inject.bind.BinderModule;

/**
 * Created by IntelliJ IDEA.
 * User: celestino
 * Date: 3/27/13
 * Time: 6:34 PM
 */
public class CoreBinder extends BinderModule {

    @Override
    protected void declare() {
        bind(ContainerManager.class).to(CargoManager.class);
        construct(ApplicationEventDispatcher.class);
        construct(OgnlContext.class);
        construct(SharedClassLoader.class);
        multibind(RemoteRequestHandler.class).to(DefaultHandler.class);
        multibind(RemoteRequestHandler.class).to(ObjectFactoryHandler.class);
        multibind(RemoteRequestHandler.class).to(MockedFieldRequestHandler.class);
        multibind(RemoteRequestHandler.class).to(CustomObjectFactoryHandler.class);
        multibind(RemoteRequestHandler.class).to(SettingsHandler.class);
        construct(HTTPServer.class);
        construct(MockedFieldsRepository.class);
        construct(ClassPoolRepository.class);
        construct(ContainersRepository.class);
        construct(CustomObjectFactoryRepository.class);
        construct(ObjectFactoryRepository.class);
        construct(SettingsRepository.class);
        construct(WebApplicationRepository.class);
        construct(TaskManager.class);
        construct(SettingsManager.class);
        construct(EventManager.class);
        construct(PropertyManager.class);
    }
}
