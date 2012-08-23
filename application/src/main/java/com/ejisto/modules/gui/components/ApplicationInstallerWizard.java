/*
 * Ejisto, a powerful developer assistant
 *
 * Copyright (C) 2010-2012  Celestino Bellone
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

package com.ejisto.modules.gui.components;

import com.ejisto.modules.gui.components.helper.Step;
import org.jdesktop.swingx.JXPanel;

import javax.swing.*;
import java.awt.*;

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
        this.setLayout(new BorderLayout(0, 0));
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
        if (cardContainer != null) {
            return cardContainer;
        }
        cardContainer = new JXPanel();
        cardManager = new CardLayout();
        cardContainer.setLayout(cardManager);
        return cardContainer;
    }

}  //  @jve:decl-index=0:visual-constraint="10,10"
