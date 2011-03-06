/*
 * Ejisto, a powerful developer assistant
 *
 * Copyright (C) 2010-2011  Celestino Bellone
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

import ch.lambdaj.function.closure.Closure1;
import com.ejisto.modules.controller.WizardException;
import com.ejisto.modules.gui.components.EjistoDialog;
import com.ejisto.modules.gui.components.ResourcesFilter;
import com.ejisto.modules.gui.components.helper.CallbackAction;
import com.ejisto.modules.gui.components.helper.Step;
import org.springframework.util.CollectionUtils;

import java.awt.event.ActionEvent;

import static ch.lambdaj.Lambda.var;
import static com.ejisto.constants.StringConstants.SELECT_ALL;
import static com.ejisto.constants.StringConstants.SELECT_NONE;
import static com.ejisto.util.GuiUtils.getMessage;

public class ClassesFilteringController extends AbstractApplicationInstallerController {

    private ResourcesFilter classesFilteringTab;
    private Closure1<ActionEvent> selectAllOrNone;

    public ClassesFilteringController(EjistoDialog dialog) {
        super(dialog);
    }

    @Override
    public ResourcesFilter getView() {
        if (classesFilteringTab != null) return classesFilteringTab;
        selectAllOrNone = new Closure1<ActionEvent>() {{
            of(ClassesFilteringController.this).actionPerformed(var(ActionEvent.class));
        }};
        CallbackAction c1 = new CallbackAction(getMessage("wizard.jarfilter.selectall.text"), SELECT_ALL.getValue(),
                                               selectAllOrNone);
        CallbackAction c2 = new CallbackAction(getMessage("wizard.jarfilter.selectnone.text"), SELECT_NONE.getValue(),
                                               selectAllOrNone);
        classesFilteringTab = new ResourcesFilter(c1, c2);
        return classesFilteringTab;
    }

    @Override
    public boolean canProceed() {
        return !CollectionUtils.isEmpty(getSession().getIncludedJars());
    }

    @Override
    public boolean isExecutionSucceeded() throws WizardException {
        return true;
    }

    @Override
    public Step getStep() {
        return Step.CLASSES_FILTERING;
    }

    @Override
    public void activate() {
        getView().setResources(getSession().getIncludedJars());
    }

    void actionPerformed(ActionEvent ev) {
        getView().select(ev.getActionCommand().equals(SELECT_ALL.getValue()));
    }

    @Override
    public boolean executionCompleted() {
        return true;
    }

    @Override
    public void beforeNext() {
        getSession().setBlacklist(getView().getBlacklistedObjects());
    }

    @Override
    public String getTitleKey() {
        return "wizard.classesfiltering.title";
    }

    @Override
    public String getDescriptionKey() {
        return "wizard.classesfiltering.description";
    }

}
