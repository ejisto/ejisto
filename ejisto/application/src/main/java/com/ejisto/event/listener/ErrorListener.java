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

package com.ejisto.event.listener;

import com.ejisto.event.def.ApplicationError;
import com.ejisto.modules.gui.Application;
import org.jdesktop.swingx.JXErrorPane;
import org.jdesktop.swingx.error.ErrorInfo;
import org.springframework.context.ApplicationListener;

import javax.annotation.Resource;
import java.util.logging.Level;

import static com.ejisto.util.GuiUtils.getMessage;

public class ErrorListener implements ApplicationListener<ApplicationError> {

    @Resource
    private Application application;

    @Override
    public void onApplicationEvent(ApplicationError event) {
        JXErrorPane.showDialog(application, getErrorInfo(event));
    }

    private ErrorInfo getErrorInfo(ApplicationError event) {
        return new ErrorInfo(getMessage("error.dialog.title"),
                             getMessage("error.dialog.message", event.getError().getClass().getName()),
                             event.getError().getMessage(), event.getPriority().name(), event.getError(), Level.SEVERE,
                             null);
    }
}
