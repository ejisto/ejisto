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

package com.ejisto.event.listener;

import java.util.logging.Level;

import org.jdesktop.swingx.JXErrorPane;
import org.jdesktop.swingx.error.ErrorInfo;
import org.springframework.context.ApplicationListener;

import com.ejisto.event.def.ApplicationError;


public class ErrorListener implements ApplicationListener<ApplicationError> {

    @Override
    public void onApplicationEvent(ApplicationError event) {
        JXErrorPane errorPane = new JXErrorPane();
        errorPane.setErrorInfo(getErrorInfo(event));
        errorPane.setVisible(true);
    }

    private ErrorInfo getErrorInfo(ApplicationError event) {
        return new ErrorInfo("Error","An error has occurred", event.getError().getMessage(), event.getPriority().name(),event.getError(), Level.SEVERE, null);
    }
}
