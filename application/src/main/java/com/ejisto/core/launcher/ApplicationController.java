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

package com.ejisto.core.launcher;

import com.ejisto.event.ApplicationListener;
import com.ejisto.event.def.ShutdownRequest;
import com.ejisto.services.Service;
import com.ejisto.services.ServiceType;
import lombok.extern.log4j.Log4j;

import java.util.List;

import static ch.lambdaj.Lambda.*;
import static org.hamcrest.Matchers.equalTo;

@Log4j
public class ApplicationController implements ApplicationListener<ShutdownRequest> {

    private final List<Service> startupServices;
    private final List<Service> shutdownServices;

    public ApplicationController(List<Service> services) {
        this.startupServices = sortServices(filterServices(services, ServiceType.STARTUP));
        this.shutdownServices = sortServices(filterServices(services, ServiceType.SHUTDOWN));
    }

    public void startup() {
        log.debug("invoking startup services...");
        forEach(startupServices, Service.class).execute();
    }

    private void shutdown() {
        log.debug("invoking startup services...");
        forEach(shutdownServices, Service.class).execute();
        log.info("Application shutdown successfully completed. Invoking shutdown hooks via System.exit(0)");
        System.exit(0);
    }

    @Override
    public void onApplicationEvent(ShutdownRequest event) {
        shutdown();
    }

    private static List<Service> sortServices(List<Service> services) {
        return sort(services, on(Service.class).getPriority());
    }

    private static List<Service> filterServices(List<Service> services, ServiceType serviceType) {
        return select(services, having(on(Service.class).getServiceType(), equalTo(serviceType)));
    }
}
