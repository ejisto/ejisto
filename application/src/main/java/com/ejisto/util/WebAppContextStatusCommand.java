/*
 * Ejisto, a powerful developer assistant
 *
 * Copyright (C) 2010-2014 Celestino Bellone
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

package com.ejisto.util;

import com.ejisto.constants.StringConstants;

import java.util.Arrays;
import java.util.Optional;

/**
* Created by IntelliJ IDEA.
* User: celestino
* Date: 4/19/14
* Time: 7:03 PM
*/
public enum WebAppContextStatusCommand {
    START(StringConstants.START_CONTEXT_COMMAND),
    STOP(StringConstants.STOP_CONTEXT_COMMAND),
    DELETE(StringConstants.DELETE_CONTEXT_COMMAND);
    private final StringConstants command;

    WebAppContextStatusCommand(StringConstants command) {
        this.command = command;
    }

    public static Optional<WebAppContextStatusCommand> fromString(String commandAsString) {
        return Arrays.stream(values())
                .filter(c -> c.command.getValue().equals(commandAsString))
                .findFirst();
    }
}
