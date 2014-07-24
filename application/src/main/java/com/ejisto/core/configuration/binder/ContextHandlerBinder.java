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

package com.ejisto.core.configuration.binder;

import com.ejisto.modules.vertx.handler.*;
import com.ejisto.modules.vertx.handler.internal.*;
import com.ejisto.modules.vertx.handler.service.ContainerService;
import com.ejisto.modules.vertx.handler.service.FieldService;
import com.ejisto.modules.vertx.handler.service.InstalledWebApplicationService;
import se.jbee.inject.bind.BinderModule;

/**
 * Created by IntelliJ IDEA.
 * User: celestino
 * Date: 4/6/14
 * Time: 11:09 AM
 */
public class ContextHandlerBinder extends BinderModule {
    @Override
    protected void declare() {
        multibind(ContextHandler.class).to(Index.class);
        multibind(ContextHandler.class).to(Resources.class);
        multibind(ContextHandler.class).to(Translations.class);
        multibind(ContextHandler.class).to(FieldService.class);
        multibind(ContextHandler.class).to(InstalledWebApplicationService.class);
        multibind(ContextHandler.class).to(ContainerService.class);
        multibind(ContextHandler.class).to(ApplicationInstallerWizardHandler.class);
        multibind(ContextHandler.class).to(CustomObjectFactoryHandler.class);
        multibind(ContextHandler.class).to(DataCollectorHandler.class);
        multibind(ContextHandler.class).to(MockedFieldHandler.class);
        multibind(ContextHandler.class).to(ObjectFactoryHandler.class);
        multibind(ContextHandler.class).to(SettingHandler.class);
    }
}
