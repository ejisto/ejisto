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

package com.ejisto.services.shutdown;

import com.ejisto.core.container.ContainerManager;
import lombok.extern.log4j.Log4j;

import javax.annotation.Resource;

@Log4j
public class ContainerShutdown extends BaseShutdownService {

    private final ContainerManager containerManager;

    public ContainerShutdown(ContainerManager containerManager) {
        this.containerManager = containerManager;
    }


    @Override
    public void execute() {
        try {
            containerManager.stopAllRunningContainers();
        } catch (Exception e) {
            log.error("error during server shutdown", e);
        }
    }

    @Override
    public int getPriority() {
        return 0;
    }

}
