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

package com.ejisto.modules.controller.wizard.installer.workers;

import com.ejisto.modules.executor.ErrorDescriptor;

/**
 * Interfaces for tasks that notify a progress to the user.
 * Created by IntelliJ IDEA.
 * User: celestino
 * Date: 8/9/12
 * Time: 8:06 AM
 */
public interface ProgressListener {

    /**
     * Notifies a "job done" message
     *
     * @param progress jobs completed by caller task
     * @param message  message to display
     */
    void progressChanged(int progress, String message);

    /**
     * Notifies an error event
     *
     * @param errorDescriptor the error detail
     */
    void errorOccurred(ErrorDescriptor errorDescriptor);
}
