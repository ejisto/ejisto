/*******************************************************************************
 * Copyright 2010 Celestino Bellone
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package com.ejisto.core.launcher;

import static ch.lambdaj.Lambda.forEach;
import static ch.lambdaj.Lambda.having;
import static ch.lambdaj.Lambda.on;
import static ch.lambdaj.Lambda.select;
import static org.hamcrest.Matchers.equalTo;

import java.util.Collection;
import java.util.List;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationListener;

import com.ejisto.event.def.ShutdownRequest;
import com.ejisto.services.Service;
import com.ejisto.services.ServiceType;

public class ApplicationController implements InitializingBean, ApplicationListener<ShutdownRequest> {

    private static Logger logger = Logger.getLogger(ApplicationController.class);

    @Resource
    private Collection<Service> services;

    public ApplicationController() {
    }

    public void startup() {
        if(logger.isDebugEnabled()) logger.debug("invoking startup services...");
        forEach(select(services, having(on(Service.class).getServiceType(), equalTo(ServiceType.STARTUP))), Service.class).execute();
    }

    public void shutdown() {
        if(logger.isDebugEnabled()) logger.debug("invoking startup services...");
        forEach(select(services, having(on(Service.class).getServiceType(), equalTo(ServiceType.SHUTDOWN))), Service.class).execute();
        logger.info("Application shutdown succesfully completed. Invoking shutdown hooks via System.exit(0)");
        System.exit(0);
    }

    public void setServices(List<Service> services) {
        this.services = services;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
    }

    @Override
    public void onApplicationEvent(ShutdownRequest event) {
        shutdown();
    }
}
