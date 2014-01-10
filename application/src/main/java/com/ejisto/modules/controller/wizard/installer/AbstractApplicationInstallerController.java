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

package com.ejisto.modules.controller.wizard.installer;

import com.ejisto.modules.controller.wizard.AbstractStepController;
import com.ejisto.modules.dao.entities.WebApplicationDescriptor;
import com.ejisto.modules.executor.TaskManager;
import com.ejisto.modules.gui.components.EjistoDialog;

public abstract class AbstractApplicationInstallerController extends AbstractStepController<WebApplicationDescriptor> {

    AbstractApplicationInstallerController(EjistoDialog dialog, TaskManager taskManager) {
        super(dialog, taskManager);
    }

    @Override
    public boolean automaticallyProceedToNextStep() {
        return false;
    }

    @Override
    public void beforeNext() {
    }

    @Override
    public boolean isBackEnabled() {
        return true;
    }

    @Override
    public boolean isForwardEnabled() {
        return true;
    }

    @Override
    public boolean validateInput() {
        return true;
    }
}
