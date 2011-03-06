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

package com.ejisto.modules.gui.components.helper;

import ch.lambdaj.function.closure.Closure0;
import ch.lambdaj.function.closure.Closure1;
import org.jdesktop.swingx.action.AbstractActionExt;
import org.springframework.util.StringUtils;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.UUID;

public class CallbackAction extends AbstractActionExt {

    private static final long serialVersionUID = -7329435808055315105L;

    private Closure1<ActionEvent> callback2;
    private Closure0 checkEnabled;
    private Closure0 callback1;

    public CallbackAction(String name, Closure1<ActionEvent> callback) {
        this(name, null, callback, null);
    }

    public CallbackAction(String name, Closure0 callback1) {
        this(name, (String) null, callback1);
    }

    public CallbackAction(String name, String command, Closure1<ActionEvent> callback) {
        this(name, command, callback, null);
    }

    public CallbackAction(String name, Closure1<ActionEvent> target, Closure0 checkEnabled) {
        this(name, null, target, checkEnabled);
    }

    public CallbackAction(String name, String command, Closure0 callback1) {
        this(name, command, null, callback1, null);
    }

    public CallbackAction(String name, Closure0 callback1, Closure0 checkEnabled) {
        this(name, null, null, callback1, checkEnabled);
    }

    public CallbackAction(String name, String command, Closure1<ActionEvent> callback2, Closure0 checkEnabled) {
        this(name, command, callback2, null, checkEnabled);
    }

    public CallbackAction(String name, String command, Closure1<ActionEvent> callback2, Closure0 callback1, Closure0 checkEnabled) {
        super(name, command);
        this.callback1 = callback1;
        this.callback2 = callback2;
        this.checkEnabled = checkEnabled;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (callback2 != null) callback2.apply(e);
        if (callback1 != null) callback1.apply();
    }

    @Override
    public boolean isEnabled() {
        if (checkEnabled != null) return (Boolean) checkEnabled.apply();
        return super.isEnabled();
    }

    @Override
    public String getActionCommand() {
        String actionCommand = String.valueOf(super.getValue(ACTION_COMMAND_KEY));
        if (StringUtils.hasText(actionCommand) || actionCommand.equals("null")) {
            actionCommand = UUID.randomUUID().toString();
            super.putValue(ACTION_COMMAND_KEY, actionCommand);
        }
        return actionCommand;
    }

    public void setIcon(Icon icon) {
        putValue(Action.SMALL_ICON, icon);
    }

}
