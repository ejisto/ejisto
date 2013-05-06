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

package com.ejisto.modules.gui.components;

import com.ejisto.constants.StringConstants;
import com.ejisto.event.def.InstallContainer;
import org.jdesktop.swingx.JXButton;
import org.jdesktop.swingx.JXLabel;
import org.jdesktop.swingx.JXPanel;
import org.jdesktop.swingx.VerticalLayout;
import org.jdesktop.swingx.action.AbstractActionExt;

import java.awt.*;
import java.awt.event.ActionEvent;

import static com.ejisto.modules.gui.components.EjistoDialog.DEFAULT_WIDTH;
import static com.ejisto.util.GuiUtils.getMessage;
import static com.ejisto.util.GuiUtils.publishEvent;

/**
 * Created by IntelliJ IDEA.
 * User: celestino
 * Date: 5/6/13
 * Time: 10:49 AM
 */
public class NoContainerInstalled extends JXPanel {

    public NoContainerInstalled() {
        init();
    }

    private void init() {
        setBackground(Color.WHITE);
        setLayout(new GridBagLayout());
        setMinimumSize(new Dimension(DEFAULT_WIDTH, ContainerTab.MINIMUM_HEIGHT));
        setPreferredSize(new Dimension(DEFAULT_WIDTH, ContainerTab.MINIMUM_HEIGHT));
        setMaximumSize(new Dimension(Short.MAX_VALUE, ContainerTab.MINIMUM_HEIGHT));
        setName(getMessage("container.installation.required"));
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.fill = GridBagConstraints.NONE;
        constraints.gridx = 0;
        constraints.gridheight = 20;
        constraints.insets = new Insets(15, 10, 15, 10);
        JXLabel text = new JXLabel(getMessage("container.installation.required.message"), JXLabel.CENTER);
        text.setLineWrap(true);
        add(text, constraints);
        add(new JXButton(new AbstractActionExt(getMessage("new.container.installation")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                publishEvent(new InstallContainer(this, StringConstants.DEFAULT_CONTAINER_ID.getValue(), true));
            }
        }), constraints);
    }
}
