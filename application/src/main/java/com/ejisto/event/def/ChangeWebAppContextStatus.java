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

package com.ejisto.event.def;

import com.ejisto.constants.StringConstants;

public class ChangeWebAppContextStatus extends BaseApplicationEvent {
    private static final long serialVersionUID = 1350522622740164683L;

    public enum WebAppContextStatusCommand {
        START(StringConstants.START_CONTEXT_COMMAND),
        STOP(StringConstants.STOP_CONTEXT_COMMAND),
        DELETE(StringConstants.DELETE_CONTEXT_COMMAND);
        private final StringConstants command;

        private WebAppContextStatusCommand(StringConstants command) {
            this.command = command;
        }

        public static WebAppContextStatusCommand fromString(String commandAsString) {
            for (WebAppContextStatusCommand statusCommand : values()) {
                if (statusCommand.command.getValue().equals(commandAsString)) {
                    return statusCommand;
                }
            }
            return null;
        }
    }

    private final WebAppContextStatusCommand command;
    private final String contextPath;

    public ChangeWebAppContextStatus(Object source, WebAppContextStatusCommand command, String contextPath) {
        super(source);
        this.command = command;
        this.contextPath = contextPath;
    }

    @Override
    public String getDescription() {
        return "";
    }

    @Override
    public String getKey() {
        return null;
    }

    public WebAppContextStatusCommand getCommand() {
        return command;
    }

    public String getContextPath() {
        return contextPath;
    }

}
