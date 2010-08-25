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

import static com.ejisto.util.GuiUtils.getMessage;
import static com.ejisto.util.SpringBridge.publishApplicationEvent;

import java.awt.event.ActionEvent;

import javax.swing.Action;
import javax.swing.ImageIcon;

import org.jdesktop.swingx.action.AbstractActionExt;
import org.springframework.util.StringUtils;

import com.ejisto.event.def.BaseApplicationEvent;


public class EjistoAction<T extends BaseApplicationEvent> extends AbstractActionExt {
    private static final long serialVersionUID = 4999338415439543233L;
    private T applicationEvent;

    public EjistoAction(T applicationEvent) {
        super();
        this.applicationEvent=applicationEvent;
        putValue(Action.NAME, getMessage(applicationEvent.getDescription()));
//        putValue(Action.SHORT_DESCRIPTION, applicationEvent.getDescription());
        putValue(Action.SMALL_ICON, getIcon(applicationEvent));
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        publishApplicationEvent(applicationEvent);
    }
    
    private ImageIcon getIcon(T applicationEvent) {
        String iconKey = applicationEvent.getIconKey();
        if(!StringUtils.hasText(iconKey)) return null;
        return new ImageIcon(getClass().getResource(getMessage(iconKey)));
    }

    
}
