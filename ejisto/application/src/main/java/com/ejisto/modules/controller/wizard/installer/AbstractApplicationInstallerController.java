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

package com.ejisto.modules.controller.wizard.installer;

import com.ejisto.core.jetty.WebApplicationDescriptor;
import com.ejisto.modules.controller.wizard.AbstractStepController;
import com.ejisto.modules.gui.components.EjistoDialog;

public abstract class AbstractApplicationInstallerController extends AbstractStepController<WebApplicationDescriptor> {

    public AbstractApplicationInstallerController(EjistoDialog dialog) {
        super(dialog);
    }
    
    @Override
    public boolean automaticallyProceedToNextStep() {
        return false;
    }
    
    @Override
    public void beforeNext() {
    }

}
