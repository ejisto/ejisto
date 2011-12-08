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
import com.ejisto.modules.executor.TaskManager;
import org.jdesktop.swingx.action.AbstractActionExt;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.concurrent.Executors;

import static com.ejisto.modules.executor.TaskManager.createNewBackgroundTask;
import static com.ejisto.util.GuiUtils.*;
import static com.ejisto.util.SpringBridge.publishApplicationEvent;

public class EjistoAction<T extends BaseApplicationEvent> extends AbstractActionExt {
    private static final long serialVersionUID = 4999338415439543233L;
    private final T applicationEvent;
    private boolean async;

    public EjistoAction(T applicationEvent) {
        this(applicationEvent, false);
    }

    public EjistoAction(T applicationEvent, boolean async) {
        super();
        this.applicationEvent = applicationEvent;
        putValue(Action.NAME, getMessage(applicationEvent.getDescription()));
        putValue(Action.SMALL_ICON, getIcon(applicationEvent));
        putValue(Action.SHORT_DESCRIPTION, getMessage(applicationEvent.getDescription()));
        this.async = async;
    }

    @Override
    public void actionPerformed(ActionEvent e) {

        Runnable action = new Runnable() {
            @Override
            public void run() {
                publishApplicationEvent(applicationEvent);
            }
        };

        if (!async) {
            runOnEDT(action);
        } else {
            TaskManager.getInstance().addNewTask(
                    createNewBackgroundTask(Executors.callable(action), applicationEvent.getDescription()));
        }
    }

    public String getKey() {
        return applicationEvent.getKey();
    }
}
