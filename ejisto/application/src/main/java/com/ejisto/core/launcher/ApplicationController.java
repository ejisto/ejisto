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

package com.ejisto.core.launcher;

import com.ejisto.event.def.ShutdownRequest;
import com.ejisto.services.Service;
import com.ejisto.services.ServiceType;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationListener;

import javax.annotation.Resource;
import java.util.Collection;

import static ch.lambdaj.Lambda.*;
import static org.hamcrest.Matchers.equalTo;

public class ApplicationController implements InitializingBean, ApplicationListener<ShutdownRequest> {

    private static Logger logger = Logger.getLogger(ApplicationController.class);

    @Resource
    private Collection<Service> services;

    public ApplicationController() {
    }

    public void startup() {
        if (logger.isDebugEnabled()) logger.debug("invoking startup services...");
        forEach(select(services, having(on(Service.class).getServiceType(), equalTo(ServiceType.STARTUP))),
                Service.class).execute();
    }

    public void shutdown() {
        if (logger.isDebugEnabled()) logger.debug("invoking startup services...");
        forEach(select(services, having(on(Service.class).getServiceType(), equalTo(ServiceType.SHUTDOWN))),
                Service.class).execute();
        logger.info("Application shutdown succesfully completed. Invoking shutdown hooks via System.exit(0)");
        System.exit(0);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
    }

    @Override
    public void onApplicationEvent(ShutdownRequest event) {
        shutdown();
    }
}
