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

package com.ejisto.event.def;

import com.ejisto.modules.dao.entities.Container;
import com.ejisto.modules.dao.entities.WebApplicationDescriptor;

/**
 * Created by IntelliJ IDEA.
 * User: celestino
 * Date: 5/15/14
 * Time: 8:20 AM
 */
public class ApplicationInstallFinalization extends BaseApplicationEvent {


    private final WebApplicationDescriptor descriptor;
    private final Container targetContainer;

    public ApplicationInstallFinalization(Object source, WebApplicationDescriptor descriptor,
                                          Container targetContainer) {
        super(source);
        this.descriptor = descriptor;
        this.targetContainer = targetContainer;
    }

    @Override
    public String getDescription() {
        return null;
    }

    @Override
    public String getKey() {
        return "ApplicationInstallFinalization";
    }

    @Override
    protected String getEventDescriptionValue() {
        return "ApplicationInstallFinalization";
    }

    public WebApplicationDescriptor getDescriptor() {
        return descriptor;
    }

    public Container getTargetContainer() {
        return targetContainer;
    }
}
