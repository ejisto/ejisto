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

import com.ejisto.modules.controller.WizardException;
import com.ejisto.modules.gui.components.EjistoDialog;
import com.ejisto.modules.gui.components.SummaryTab;
import com.ejisto.modules.gui.components.helper.Step;

public class SummaryController extends AbstractApplicationInstallerController {

    private SummaryTab summaryTab;

    public SummaryController(EjistoDialog dialog) {
        super(dialog);
    }

    @Override
    public SummaryTab getView() {
        if (this.summaryTab != null) {
            return this.summaryTab;
        }
        summaryTab = new SummaryTab();
        return summaryTab;
    }

    @Override
    public boolean canProceed() {
        return true;
    }

    @Override
    public boolean isExecutionSucceeded() throws WizardException {
        return true;
    }

    @Override
    public Step getStep() {
        return Step.SUMMARY;
    }

    @Override
    public void activate() {
        getView().renderMockedFields(getSession().getModifiedFields());
    }

    @Override
    public String getTitleKey() {
        return "wizard.summary.title";
    }

    @Override
    public String getDescriptionKey() {
        return "wizard.summary.description";
    }
}
