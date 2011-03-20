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

package com.ejisto.event.def;

import com.ejisto.constants.StringConstants;

public class ChangeServerStatus extends BaseApplicationEvent {
    private static final long serialVersionUID = 62223689929514687L;
    private Command command;

    public enum Command {
        STARTUP("start.default.server", "server.start.icon"),
        SHUTDOWN("stop.default.server", "server.stop.icon");
        private String description;
        private String icon;

        private Command(String description, String icon) {
            this.description = description;
            this.icon = icon;
        }

        public String getDescription() {
            return description;
        }

        public String getIcon() {
            return icon;
        }
    }

    public ChangeServerStatus(Object source, Command command) {
        super(source);
        this.command = command;
    }

    public Command getCommand() {
        return command;
    }

    @Override
    public String toString() {
        return command + " received from: " + source;
    }

    @Override
    public String getDescription() {
        return command.getDescription();
    }

    @Override
    public String getIconKey() {
        return command.getIcon();
    }

    @Override
    public String getKey() {
        return command == Command.STARTUP ? StringConstants.START_CONTAINER.getValue() : StringConstants.STOP_CONTAINER.getValue();
    }
}
