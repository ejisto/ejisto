/*
 * Copyright 2010 Celestino Bellone
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions
 * and limitations under the License.
 */

package com.ejisto.constants;

public enum StringConstants {
    JETTY_HOME_DIR("jetty.home"),
    JETTY_WEBAPPS_DIR("jetty.webapps.dir"),
    MAIN_TITLE("main.title"),
    DERBY_SCRIPT("derby.script"),
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
}
