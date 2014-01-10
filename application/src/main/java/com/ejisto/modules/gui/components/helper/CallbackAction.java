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

package com.ejisto.modules.gui.components.helper;

import org.apache.commons.lang3.StringUtils;
import org.jdesktop.swingx.action.AbstractActionExt;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.UUID;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

public class CallbackAction extends AbstractActionExt {

    private static final long serialVersionUID = -7329435808055315105L;

    private final transient Consumer<ActionEvent> callback;
    private final transient BooleanSupplier checkEnabled;

    public CallbackAction(String name, Consumer<ActionEvent> callback) {
        this(name, null, callback, null);
    }

    public CallbackAction(String name, String command, Consumer<ActionEvent> callback, BooleanSupplier checkEnabled) {
        super(name, command);
        this.callback = callback;
        this.checkEnabled = checkEnabled;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        callback.accept(e);
    }

    @Override
    public boolean isEnabled() {
        if (checkEnabled != null) {
            return checkEnabled.getAsBoolean();
        }
        return super.isEnabled();
    }

    @Override
    public String getActionCommand() {
        String actionCommand = String.valueOf(super.getValue(ACTION_COMMAND_KEY));
        if (StringUtils.isNotBlank(actionCommand) || actionCommand.equals("null")) {
            actionCommand = UUID.randomUUID().toString();
            super.putValue(ACTION_COMMAND_KEY, actionCommand);
        }
        return actionCommand;
    }

    public void setIcon(Icon icon) {
        putValue(Action.SMALL_ICON, icon);
    }

}
