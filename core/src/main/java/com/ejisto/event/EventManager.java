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

package com.ejisto.event;

import com.ejisto.event.def.ApplicationError;
import com.ejisto.event.def.BaseApplicationEvent;
import com.ejisto.modules.executor.TaskManager;
import lombok.extern.log4j.Log4j;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.concurrent.Callable;

import static com.ejisto.constants.StringConstants.GUI_TASK_EXCEPTION_PROPERTY;
import static com.ejisto.event.def.ApplicationError.Priority.HIGH;
import static com.ejisto.modules.executor.TaskManager.createNewGuiTask;

@Log4j
public class EventManager {
    private final TaskManager taskManager;
    private final ApplicationEventDispatcher applicationEventDispatcher;

    public EventManager(TaskManager taskManager,
                        ApplicationEventDispatcher applicationEventDispatcher) {
        this.taskManager = taskManager;
        this.applicationEventDispatcher = applicationEventDispatcher;
    }

    public void publishEvent(final BaseApplicationEvent event) {
        taskManager.addNewTask(createNewGuiTask(new Callable<Void>() {
            @Override
            public Void call() {
                publishEventAndWait(event);
                return null;
            }
        }, event.toString(), listener));
    }

    public void publishEventAndWait(BaseApplicationEvent event) {
        applicationEventDispatcher.broadcastEvent(event);
    }

    private final PropertyChangeListener listener = new PropertyChangeListener() {
        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if (evt.getPropertyName().equals(GUI_TASK_EXCEPTION_PROPERTY.getValue())) {
                publishEventAndWait(new ApplicationError(this, HIGH, (Exception) evt.getNewValue()));
            }
        }
    };

}
