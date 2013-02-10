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

package com.ejisto.modules.cargo;

import com.ejisto.core.container.WebApplication;
import org.codehaus.cargo.container.deployable.Deployable;

import static com.ejisto.core.container.WebApplication.Status.STARTED;

/**
 * Created by IntelliJ IDEA.
 * User: celestino
 * Date: 5/22/11
 * Time: 3:10 PM
 */
public class CargoWebApplication implements WebApplication<Deployable> {

    private final String context;
    private final Deployable webApplication;
    private Status status;
    private final String containerId;

    public CargoWebApplication(String context, String containerId, Deployable webApplication) {
        this.context = context;
        this.webApplication = webApplication;
        this.containerId = containerId;
    }

    @Override
    public Deployable getContainerWebApplicationDescriptor() {
        return webApplication;
    }

    @Override
    public String getWebApplicationContextPath() {
        return context;
    }

    @Override
    public Status getStatus() {
        return status;
    }

    @Override
    public void setStatus(Status status) {
        this.status = status;
    }

    @Override
    public boolean isRunning() {
        return status == STARTED;
    }

    @Override
    public String getContainerId() {
        return containerId;
    }
}
