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

import com.ejisto.event.ApplicationEventDispatcher;
import com.ejisto.event.ApplicationListener;
import com.ejisto.event.def.ShutdownRequest;
import com.ejisto.services.Service;
import com.ejisto.services.ServiceType;
import lombok.extern.log4j.Log4j;

import java.util.List;
import java.util.stream.Collectors;

@Log4j
public class ApplicationController implements ApplicationListener<ShutdownRequest> {

    private final List<Service> startupServices;
    private final List<Service> shutdownServices;
    private final ApplicationEventDispatcher eventDispatcher;

    public ApplicationController(List<Service> services, ApplicationEventDispatcher eventDispatcher) {
        this.eventDispatcher = eventDispatcher;
        this.startupServices = sortServices(filterServices(services, ServiceType.STARTUP));
        this.shutdownServices = sortServices(filterServices(services, ServiceType.SHUTDOWN));
    }

    public void startup() {
        log.debug("registering shutdown hook...");
        eventDispatcher.registerApplicationEventListener(this);
        log.debug("invoking startup services...");
        startupServices.stream().forEach(Service::execute);

    }

    private void shutdown() {
        log.debug("invoking shutdown services...");
        shutdownServices.stream().forEach(Service::execute);
        log.info("Application shutdown successfully completed. Invoking shutdown hooks via System.exit(0)");
        System.exit(0);
    }

    @Override
    public void onApplicationEvent(ShutdownRequest event) {
        shutdown();
    }

    @Override
    public Class<ShutdownRequest> getTargetEventType() {
        return ShutdownRequest.class;
    }

    private static List<Service> sortServices(List<Service> services) {
        return services.stream().sorted().collect(Collectors.toList());
    }

    private static List<Service> filterServices(List<Service> services, ServiceType serviceType) {
        return services.stream().filter(service -> service.getServiceType().equals(serviceType)).collect(
                Collectors.toList());
    }
}
