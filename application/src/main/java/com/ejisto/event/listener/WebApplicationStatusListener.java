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
import com.ejisto.event.def.ChangeWebAppStatus;
import com.ejisto.event.def.WebAppContextStatusChanged;
import com.ejisto.modules.cargo.NotInstalledException;
import com.ejisto.modules.repository.MockedFieldsRepository;
import com.ejisto.util.IOUtils;
import com.ejisto.util.WebAppContextStatusCommand;
import lombok.extern.log4j.Log4j;

import java.nio.file.Paths;
import java.util.Optional;

import static com.ejisto.constants.StringConstants.DEFAULT_CONTAINER_ID;
import static com.ejisto.constants.StringConstants.DEPLOYABLES_DIR;

/**
 * Created by IntelliJ IDEA.
 * User: celestino
 * Date: 4/19/14
 * Time: 7:45 PM
 */
@Log4j
public class WebApplicationStatusListener implements ApplicationListener<ChangeWebAppStatus> {

    private final ContainerManager containerManager;
    private final MockedFieldsRepository mockedFieldsRepository;
    private final EventManager eventManager;

    public WebApplicationStatusListener(ContainerManager containerManager,
                                        MockedFieldsRepository mockedFieldsRepository,
                                        EventManager eventManager) {
        this.containerManager = containerManager;
        this.mockedFieldsRepository = mockedFieldsRepository;
        this.eventManager = eventManager;
    }

    @Override
    public void onApplicationEvent(ChangeWebAppStatus event) {
        String containerId = Optional.ofNullable(event.getContainerId()).orElse(DEFAULT_CONTAINER_ID.getValue());
        String contextPath = Optional.ofNullable(event.getContextPath()).orElseThrow(IllegalArgumentException::new);
        try {
            switch(event.getCommand()) {
                case START:
                    startWebapp(containerId, contextPath);
                    break;
                case STOP:
                    stopWebapp(containerId, contextPath);
                    break;
                case DELETE:
                    undeployExistingWebapp(containerId, contextPath);
                    break;
            }
        } catch (NotInstalledException e) {
            throw new IllegalStateException(e);
        }
    }

    private void undeployExistingWebapp(String containerId, String contextPath) throws NotInstalledException {
        log.info("undeploying webapp " + contextPath);
        if (containerManager.undeploy(containerId, contextPath)) {
            mockedFieldsRepository.deleteContext(contextPath);
            IOUtils.deleteFile(Paths.get(System.getProperty(DEPLOYABLES_DIR.getValue()), contextPath).toFile());
            eventManager.publishEvent(new WebAppContextStatusChanged(this, WebAppContextStatusCommand.DELETE, contextPath));
            log.info("webapp " + contextPath + " undeployed");
        }
    }

    private void startWebapp(String containerId, String contextPath) throws NotInstalledException {
        log.info("starting webapp " + contextPath);
        if (containerManager.startWebApplication(containerId, contextPath)) {
            log.info("started webapp " + contextPath);
        }
    }

    private void stopWebapp(String containerId, String contextPath) throws NotInstalledException {
        log.info("stopping webapp " + contextPath);
        if (containerManager.stopWebApplication(containerId, contextPath)) {
            log.info("stopped webapp " + contextPath);
        }
    }

    @Override
    public Class<ChangeWebAppStatus> getTargetEventType() {
        return ChangeWebAppStatus.class;
    }
}
