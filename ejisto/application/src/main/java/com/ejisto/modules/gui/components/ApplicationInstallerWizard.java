package com.ejisto.modules.gui.components;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;

import javax.swing.JPanel;

import org.jdesktop.swingx.JXPanel;

import com.ejisto.modules.gui.components.helper.Step;

public class ApplicationInstallerWizard extends JXPanel {

	private static final long serialVersionUID = 788313444893972067L;
	private JXPanel content = null;
	private JXPanel cardContainer;
	private CardLayout cardManager;

	public ApplicationInstallerWizard() {
		super();
		initialize();
	}
	
	public void addTab(JPanel tab, Step step) {
		cardContainer.add(tab, step.name());
	}
	
	public void goToStep(Step step) {
        cardManager.show(cardContainer, step.name());
    }

	public void previousStep(Step step) {
	    cardManager.show(cardContainer, step.name());
    }
	
	private void initialize() {
        this.setLayout(new BorderLayout(0,0));
        this.setSize(new Dimension(600, 500));
        this.setMinimumSize(new Dimension(500, 400));
        this.add(getContent(), BorderLayout.CENTER);
	}
	
	/**
	 * This method initializes content	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JXPanel getContent() {
		if (content == null) {
			BorderLayout borderLayout = new BorderLayout();
			borderLayout.setHgap(20);
			borderLayout.setVgap(20);
			content = new JXPanel();
			content.setLayout(borderLayout);
			content.add(getCardContainer(), BorderLayout.CENTER);
		}
		return content;
	}
	
	private JXPanel getCardContainer() {
	    if(cardContainer != null) return cardContainer;
	    cardContainer = new JXPanel();
	    cardManager = new CardLayout();
	    cardContainer.setLayout(cardManager);
	    return cardContainer;
	}
	
    
	
}  //  @jve:decl-index=0:visual-constraint="10,10"
