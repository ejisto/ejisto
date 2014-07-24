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

package com.ejisto.event.def;

import com.ejisto.util.WebAppContextStatusCommand;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Created by IntelliJ IDEA.
 * User: celestino
 * Date: 4/19/14
 * Time: 7:45 PM
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ChangeWebAppStatus extends BaseApplicationEvent {

    private final String containerId;
    private final String contextPath;
    private final WebAppContextStatusCommand command;

    public ChangeWebAppStatus(Object source,
                              String containerId,
                              String contextPath,
                              WebAppContextStatusCommand command) {
        super(source);
        this.containerId = containerId;
        this.contextPath = contextPath;
        this.command = command;
    }

    @Override
    public String getDescription() {
        return command.name();
    }

    @Override
    public String getKey() {
        return null;
    }

    @Override
    protected String getEventDescriptionValue() {
        return null;
    }
}
