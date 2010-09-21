/*
 * Ejisto, a powerful developer assistant
 *
 * Copyright (C) 2010  Celestino Bellone
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

package com.ejisto.constants;

public enum StringConstants {
    JETTY_HOME_DIR("jetty.home"),
    JETTY_WEBAPPS_DIR("jetty.webapps.dir"),
    MAIN_TITLE("main.title"),
    APPLICATION_WIDTH("application.dimension.width"),
    APPLICATION_HEIGHT("application.dimension.height"),
    LAST_FILESELECTION_PATH("fileselection.last.path"),
    DERBY_SCRIPT("derby.script"),
    DESCRIPTOR_DIR("descriptor.dir"),
    INITIALIZE_DATABASE("INITIALIZE_DATABASE"),
    SELECT_FILE_COMMAND("selectFile"),
    PREVIOUS_STEP_COMMAND("previous"),
    NEXT_STEP_COMMAND("next"),
    SELECT_ALL("selectAll"),
    SELECT_NONE("selectNone"),
    CLOSE("close"),
    CONFIRM("confirm"),
    START_JETTY("startJetty"),
    STOP_JETTY("stopJetty"),
    SHUTDOWN("shutdown"),
    LOAD_WEB_APP("loadwebapp"),
    CONTEXT_PREFIX_SEPARATOR("!!registeredContext!!"),
    START_CONTEXT_COMMAND("start"),
    START_CONTEXT_PREFIX("start!!registeredContext!!"),
    STOP_CONTEXT_COMMAND("stop"),
    STOP_CONTEXT_PREFIX("stop!!registeredContext!!"),
    DELETE_CONTEXT_COMMAND("delete"),
    DELETE_CONTEXT_PREFIX("delete!!registeredContext!!"),
    ;


    private String value;

    private StringConstants(String value) {
        this.value=value;
    }

    @Override
    public String toString() {
        return value;
    }

    public String getValue() {
        return value;
    }
    
    public static StringConstants fromValue(String value) {
    	for (StringConstants constant : values()) {
			if(constant.getValue().equals(value)) return constant;
		}
    	return null;
    }
}
