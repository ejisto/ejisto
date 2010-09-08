/*******************************************************************************
 * Copyright 2010 Celestino Bellone
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package com.ejisto.event.def;

import com.ejisto.constants.StringConstants;


public class ChangeServerStatus extends BaseApplicationEvent {
    private static final long serialVersionUID = 62223689929514687L;
    private Command command;

    public enum Command {
        STARTUP("start.jetty.server", "jetty.start.icon"), 
        SHUTDOWN("stop.jetty.server", "jetty.stop.icon");
        private String description;
        private String icon;

        private Command(String description, String icon) {
            this.description = description;
            this.icon=icon;
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
        return command == Command.STARTUP ? StringConstants.START_JETTY.getValue() : StringConstants.STOP_JETTY.getValue();
    }
}
