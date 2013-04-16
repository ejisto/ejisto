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

package com.ejisto.modules.controller;

import com.ejisto.modules.executor.ExecutionState;
import com.ejisto.modules.executor.TaskDescriptor;
import com.ejisto.modules.executor.TaskManager;
import com.ejisto.modules.gui.components.TaskView;

import java.util.List;

import static ch.lambdaj.Lambda.*;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.hamcrest.Matchers.equalTo;

/**
 * Created by IntelliJ IDEA.
 * User: celestino
 * Date: 4/10/11
 * Time: 4:15 PM
 */
public class TaskController {
    private TaskView view;
    private final TaskManager taskManager;

    public TaskController(TaskManager taskManager) {
        this.taskManager = taskManager;
        taskManager.scheduleTaskAtFixedRate(new Runnable() {
            @Override
            public void run() {
                updateTasks();
            }
        }, 500, 500, SECONDS);
    }

    public TaskView getView() {
        if (view == null) {
            view = new TaskView();
        }
        view.setMinimized(true);
        return view;
    }

    private void updateTasks() {
        List<TaskDescriptor> tasks = taskManager.getRegisteredTasks();
        int completed = 0;
        int total = 0;
        if (tasks.size() > 0) {
            completed = select(tasks, having(on(TaskDescriptor.class).getExecutionState(),
                                             equalTo(ExecutionState.DONE))).size();
            total = tasks.size();
        }
        getView().setCurrentStatus(completed, total);
    }
}
