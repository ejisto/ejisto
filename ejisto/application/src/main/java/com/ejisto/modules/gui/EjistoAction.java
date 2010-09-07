/*
 * Copyright 2010 Celestino Bellone
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions
 * and limitations under the License.
 */

package com.ejisto.modules.gui;

import static com.ejisto.util.GuiUtils.getIcon;
import static com.ejisto.util.GuiUtils.getMessage;
import static com.ejisto.util.SpringBridge.publishApplicationEvent;

import java.awt.event.ActionEvent;

import javax.swing.Action;
import javax.swing.SwingUtilities;

import org.jdesktop.swingx.action.AbstractActionExt;

import com.ejisto.event.def.BaseApplicationEvent;


public class EjistoAction<T extends BaseApplicationEvent> extends AbstractActionExt {
    private static final long serialVersionUID = 4999338415439543233L;
    private T applicationEvent;

    public EjistoAction(T applicationEvent) {
        super();
        this.applicationEvent=applicationEvent;
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
