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

package com.ejisto.modules.gui;

import com.ejisto.event.def.BaseApplicationEvent;
import org.jdesktop.swingx.action.AbstractActionExt;

import javax.swing.*;
import java.awt.event.ActionEvent;

import static com.ejisto.util.GuiUtils.getIcon;
import static com.ejisto.util.GuiUtils.getMessage;
import static com.ejisto.util.SpringBridge.publishApplicationEvent;

public class EjistoAction<T extends BaseApplicationEvent> extends AbstractActionExt {
    private static final long serialVersionUID = 4999338415439543233L;
    private T applicationEvent;

    public EjistoAction(T applicationEvent) {
        super();
        this.applicationEvent = applicationEvent;
        putValue(Action.NAME, getMessage(applicationEvent.getDescription()));
        putValue(Action.SMALL_ICON, getIcon(applicationEvent));
        putValue(Action.SHORT_DESCRIPTION, getMessage(applicationEvent.getDescription()));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                publishApplicationEvent(applicationEvent);
            }
        });
    }

    public String getKey() {
        return applicationEvent.getKey();
    }
}
