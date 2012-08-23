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

package com.ejisto.core.launcher;

import com.ejisto.event.def.ShutdownRequest;
import com.ejisto.services.Service;
import com.ejisto.services.ServiceType;
import lombok.extern.log4j.Log4j;
import org.springframework.context.ApplicationListener;

import javax.annotation.Resource;
import java.util.Collection;

import static ch.lambdaj.Lambda.*;
import static org.hamcrest.Matchers.equalTo;

@Log4j
public class ApplicationController implements ApplicationListener<ShutdownRequest> {

    @Resource
    private Collection<Service> services;

    public ApplicationController() {
    }

    public void startup() {
        log.debug("invoking startup services...");
        forEach(select(services, having(on(Service.class).getServiceType(), equalTo(ServiceType.STARTUP))),
                Service.class).execute();
    }

    private void shutdown() {
        log.debug("invoking startup services...");
        forEach(select(services, having(on(Service.class).getServiceType(), equalTo(ServiceType.SHUTDOWN))),
                Service.class).execute();
        log.info("Application shutdown successfully completed. Invoking shutdown hooks via System.exit(0)");
        System.exit(0);
    }

    @Override
    public void onApplicationEvent(ShutdownRequest event) {
        shutdown();
    }
}
