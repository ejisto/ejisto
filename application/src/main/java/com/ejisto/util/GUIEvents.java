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

import com.ejisto.event.def.ChangeServerStatus;
import com.ejisto.event.def.ChangeWebAppStatus;
import org.vertx.java.core.json.JsonObject;

/**
 * Created by IntelliJ IDEA.
 * User: celestino
 * Date: 4/18/14
 * Time: 8:59 PM
 */
public class GUIEvents {

    public static ChangeServerStatus startServer(Object source, JsonObject properties) {
        return new ChangeServerStatus(source, properties.getString("containerId"), ChangeServerStatus.Command.STARTUP);
    }

    public static ChangeServerStatus stopServer(Object source, JsonObject properties) {
        return new ChangeServerStatus(source, properties.getString("containerId"), ChangeServerStatus.Command.SHUTDOWN);
    }

    public static ChangeWebAppStatus startApplication(Object source, JsonObject properties) {
        return new ChangeWebAppStatus(source, properties.getString("containerId"),
                                             properties.getString("contextPath"),
                                             WebAppContextStatusCommand.START);
    }

    public static ChangeWebAppStatus stopApplication(Object source, JsonObject properties) {
        return new ChangeWebAppStatus(source, properties.getString("containerId"),
                                      properties.getString("contextPath"),
                                      WebAppContextStatusCommand.STOP);
    }

    public static ChangeWebAppStatus deleteApplication(Object source, JsonObject properties) {
        return new ChangeWebAppStatus(source, properties.getString("containerId"),
                                      properties.getString("contextPath"),
                                      WebAppContextStatusCommand.DELETE);
    }
}
