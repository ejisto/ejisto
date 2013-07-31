/*
 * Ejisto, a powerful developer assistant
 *
 * Copyright (C) 2010-2013 Celestino Bellone
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

package com.ejisto.event.listener;

import com.ejisto.event.ApplicationListener;
import com.ejisto.event.def.ApplicationError;
import com.ejisto.modules.gui.Application;
import com.ejisto.util.GuiUtils;
import lombok.extern.log4j.Log4j;
import org.jdesktop.swingx.JXErrorPane;
import org.jdesktop.swingx.error.ErrorInfo;

import java.util.logging.Level;

import static com.ejisto.util.GuiUtils.getMessage;

@Log4j
public class ErrorListener implements ApplicationListener<ApplicationError> {

    private final Application application;

    public ErrorListener(Application application) {
        this.application = application;
    }

    @Override
    public void onApplicationEvent(ApplicationError event) {
        JXErrorPane.showDialog(application, getErrorInfo(event));
        log.error(event.getDescription(), event.getError());
    }

    @Override
    public Class<ApplicationError> getTargetEventType() {
        return ApplicationError.class;
    }

    private ErrorInfo getErrorInfo(ApplicationError event) {
        Throwable throwable = GuiUtils.getRootThrowable(event.getError());
        return new ErrorInfo(getMessage("error.dialog.title"),
                             getMessage("error.dialog.message", throwable.getClass().getName()), null,
                             event.getPriority().name(), throwable, Level.SEVERE, null);
    }

}
