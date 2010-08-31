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
        if(this.summaryTab != null) return this.summaryTab;
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
    public boolean executionCompleted() {
        return true;
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
