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

public class ChangeWebAppContextStatus extends BaseApplicationEvent {
    private static final long serialVersionUID = 1350522622740164683L;
    
    public enum WebAppContextStatusCommand {
        START(StringConstants.START_CONTEXT_COMMAND),
        STOP(StringConstants.STOP_CONTEXT_COMMAND),
        DELETE(StringConstants.DELETE_CONTEXT_COMMAND);
        private StringConstants command;
        private WebAppContextStatusCommand(StringConstants command) {
            this.command=command;
        }
        
        public static WebAppContextStatusCommand fromString(String commandAsString) {
            for (WebAppContextStatusCommand statusCommand : values()) {
                if(statusCommand.command.getValue().equals(commandAsString))
                    return statusCommand;
            }
            return null;
        }
    }

    private WebAppContextStatusCommand command;
    private String contextPath;

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
