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

package com.ejisto.services.startup;

import com.ejisto.modules.executor.TaskManager;
import com.ejisto.util.StaticData;
import lombok.extern.log4j.Log4j;

import java.util.Collections;
import java.util.List;

import static java.util.concurrent.TimeUnit.SECONDS;

/**
 * Created by IntelliJ IDEA.
 * User: celestino
 * Date: 6/2/11
 * Time: 4:17 PM
 */
@Log4j
public class TaskInitializer extends BaseStartupService {
    private final TaskManager taskManager;
    private final List<TaskManager.Descriptor> taskDescriptors;

    public TaskInitializer(TaskManager taskManager) {
        this.taskManager = taskManager;
        this.taskDescriptors = Collections.unmodifiableList(StaticData.TASK_DESCRIPTORS);
    }

    @Override
    public void execute() {
        log.info("scheduling tasks for execution");
        taskDescriptors.forEach(d -> taskManager
                .scheduleTaskAtFixedRate(d.getTask(), d.getInitialDelay(), d.getPeriod(), SECONDS));
    }

    @Override
    public int getPriority() {
        return NORMAL_PRIORITY;
    }
}
