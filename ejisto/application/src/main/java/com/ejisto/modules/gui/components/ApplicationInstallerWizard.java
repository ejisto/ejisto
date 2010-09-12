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
