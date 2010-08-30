package com.ejisto.modules.controller.wizard.installer;

import static ch.lambdaj.Lambda.var;
import static com.ejisto.constants.StringConstants.SELECT_ALL;
import static com.ejisto.constants.StringConstants.SELECT_NONE;
import static com.ejisto.util.GuiUtils.getMessage;

import java.awt.event.ActionEvent;

import org.springframework.util.CollectionUtils;

import ch.lambdaj.function.closure.Closure1;

import com.ejisto.modules.controller.WizardException;
import com.ejisto.modules.gui.components.EjistoDialog;
import com.ejisto.modules.gui.components.ResourcesFilter;
import com.ejisto.modules.gui.components.helper.CallbackAction;
import com.ejisto.modules.gui.components.helper.Step;

public class ClassesFilteringController extends AbstractApplicationInstallerController {

        
    private ResourcesFilter classesFilteringTab;
    private Closure1<ActionEvent> selectAllOrNone;

    public ClassesFilteringController(EjistoDialog dialog) {
        super(dialog);
    }

    @Override
    public ResourcesFilter getView() {
        if (classesFilteringTab != null) return classesFilteringTab;
        selectAllOrNone = new Closure1<ActionEvent>() {{ of(ClassesFilteringController.this).actionPerformed(var(ActionEvent.class)); }};
        CallbackAction c1 = new CallbackAction(getMessage("wizard.jarfilter.selectall.text"), SELECT_ALL.getValue(), selectAllOrNone);
        CallbackAction c2 = new CallbackAction(getMessage("wizard.jarfilter.selectnone.text"), SELECT_NONE.getValue(), selectAllOrNone);
        classesFilteringTab = new ResourcesFilter(c1,c2);
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

}
