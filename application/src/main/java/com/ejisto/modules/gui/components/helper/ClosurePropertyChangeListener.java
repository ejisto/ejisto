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

package com.ejisto.modules.gui.components.helper;

import ch.lambdaj.function.closure.Closure0;
import ch.lambdaj.function.closure.Closure1;
import lombok.extern.log4j.Log4j;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: celestino
 * Date: 2/12/11
 * Time: 7:34 PM
 */
@Log4j
public class ClosurePropertyChangeListener implements PropertyChangeListener, DocumentListener {

    private final String propertyName;
    private final Map<String, Closure1<String>> actionMap;
    private final Closure0 callBackAction;

    public ClosurePropertyChangeListener(String propertyName, Map<String, Closure1<String>> actionMap, Closure0 callBackAction) {
        this.propertyName = propertyName;
        this.actionMap = actionMap;
        this.callBackAction = callBackAction;
    }

    public ClosurePropertyChangeListener(String propertyName, Closure1<String> action, Closure0 callBackAction) {
        this.propertyName = propertyName;
        this.actionMap = new HashMap<>();
        this.actionMap.put(propertyName, action);
        this.callBackAction = callBackAction;
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        firePropertyChange(String.valueOf(evt.getNewValue()));
    }

    @Override
    public void insertUpdate(DocumentEvent evt) {
        try {
            firePropertyChange(evt.getDocument().getText(0, evt.getDocument().getLength()));
        } catch (BadLocationException e) {
            log.error("error during property change notification", e);
        }
    }

    @Override
    public void removeUpdate(DocumentEvent evt) {
        try {
            firePropertyChange(evt.getDocument().getText(0, evt.getDocument().getLength()));
        } catch (BadLocationException e) {
            log.error("error during property change notification", e);
        }
    }

    @Override
    public void changedUpdate(DocumentEvent evt) {
        try {
            firePropertyChange(evt.getDocument().getText(0, evt.getDocument().getLength()));
        } catch (BadLocationException e) {
            log.error("error during property change notification", e);
        }
    }

    private void firePropertyChange(String newValue) {
        actionMap.get(propertyName).apply(newValue);
        if (callBackAction != null) {
            callBackAction.apply();
        }
    }
}
