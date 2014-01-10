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
import com.ejisto.modules.executor.TaskManager;
import com.ejisto.modules.gui.components.EjistoDialog;
import com.ejisto.modules.gui.components.ResourcesFilter;
import com.ejisto.modules.gui.components.helper.CallbackAction;
import com.ejisto.modules.gui.components.helper.Step;
import org.apache.commons.collections.CollectionUtils;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.function.Consumer;

import static com.ejisto.constants.StringConstants.SELECT_ALL;
import static com.ejisto.constants.StringConstants.SELECT_NONE;
import static com.ejisto.util.GuiUtils.getMessage;
import static com.ejisto.util.GuiUtils.showWarning;
import static com.ejisto.util.LambdaUtil.callActionPerformed;

public class ClassesFilteringController extends AbstractApplicationInstallerController implements ActionListener {

    private ResourcesFilter classesFilteringTab;

    public ClassesFilteringController(EjistoDialog dialog, TaskManager taskManager) {
        super(dialog, taskManager);
    }

    @Override
    public ResourcesFilter getView() {
        if (classesFilteringTab != null) {
            return classesFilteringTab;
        }
        Consumer<ActionEvent> selectAllOrNone = callActionPerformed(this);
        CallbackAction c1 = new CallbackAction(getMessage("wizard.jarfilter.selectall.text"), SELECT_ALL.getValue(),
                                               selectAllOrNone, null);
        CallbackAction c2 = new CallbackAction(getMessage("wizard.jarfilter.selectnone.text"), SELECT_NONE.getValue(),
                                               selectAllOrNone, null);
        classesFilteringTab = new ResourcesFilter(c1, c2);
        return classesFilteringTab;
    }

    @Override
    public boolean canProceed() {
        return CollectionUtils.isNotEmpty(getSession().getIncludedJars());
    }

    @Override
    public boolean isExecutionSucceeded() {
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

    @Override
    public void actionPerformed(ActionEvent ev) {
        boolean selectAll = ev.getActionCommand().equals(SELECT_ALL.getValue());
        int libs = getSession().getIncludedJars().size();
        if (selectAll && libs > 10 && !showWarning(getView(), "wizard.classesfiltering.warning", libs)) {
            return;
        }
        getView().select(selectAll);
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
