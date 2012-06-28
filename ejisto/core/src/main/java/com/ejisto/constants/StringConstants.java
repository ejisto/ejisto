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

package com.ejisto.constants;

public enum StringConstants {
    LIB_DIR("lib.dir"),
    CONTAINERS_HOME_DIR("containers.home"),
    MAIN_TITLE("main.title"),
    APPLICATION_WIDTH("application.dimension.width"),
    APPLICATION_HEIGHT("application.dimension.height"),
    APPLICATION_MAXIMIZED("application.maximized"),
    LAST_FILESELECTION_PATH("fileselection.last.path"),
    EXTENSIONS_DIR("extensions.dir"),
    DB_SCRIPT("derby.script"),
    DESCRIPTOR_DIR("descriptor.dir"),
    INITIALIZE_DATABASE("INITIALIZE_DATABASE"),
    SELECT_FILE_COMMAND("selectFile"),
    PREVIOUS_STEP_COMMAND("previous"),
    NEXT_STEP_COMMAND("next"),
    SELECT_ALL("selectAll"),
    SELECT_NONE("selectNone"),
    CLOSE("close"),
    CONFIRM("confirm"),
    START_CONTAINER("startContainer"),
    STOP_CONTAINER("stopContainer"),
    SHUTDOWN("shutdown"),
    SHOW_ABOUT_PANEL("show-about-panel"),
    LOAD_WEB_APP("loadwebapp"),
    CONTEXT_PREFIX_SEPARATOR("!!registeredContext!!"),
    START_CONTEXT_COMMAND("start"),
    START_CONTEXT_PREFIX("!!registeredContext!!start!!registeredContext!!"),
    STOP_CONTEXT_COMMAND("stop"),
    STOP_CONTEXT_PREFIX("!!registeredContext!!stop!!registeredContext!!"),
    DELETE_CONTEXT_COMMAND("delete"),
    DELETE_CONTEXT_PREFIX("!!registeredContext!!delete!!registeredContext!!"),
    CONTEXT_PARAM_NAME("ejisto-target-context-path"),
    DEPLOYABLES_DIR("ejisto.deployables.dir"),
    RUNTIME_DIR("ejisto.runtime.dir"),
    DEFAULT_SERVER_PORT("ejisto.default.server.port"),
    EJISTO_VERSION("ejisto.version"),
    EJISTO_CLASS_TRANSFORMER_CATEGORY("EjistoClassTransformer"),
    DEFAULT_CONTAINER_ID("tomcat7x"),
    DEFAULT_CONTAINER_DESCRIPTION("container.default.description"),
    DEFAULT_CONTAINER_DOWNLOAD_URL("container.default.url"),
    DATABASE_PORT("database.port"),
    DATABASE_USER("embedded.database.username"),
    DATABASE_PWD("embedded.database.password"),
    HTTP_LISTEN_PORT("http.listen.port");

    private final String value;

    private StringConstants(String value) {
        this.value = value;
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
            if (constant.getValue().equals(value)) {
                return constant;
            }
        }
        return null;
    }
}
