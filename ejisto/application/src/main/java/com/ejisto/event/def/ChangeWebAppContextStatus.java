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
