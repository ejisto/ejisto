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

package com.ejisto.services.startup;

import com.ejisto.modules.executor.TaskDescriptor;
import com.ejisto.modules.executor.TaskManager;
import org.apache.log4j.Logger;

import javax.annotation.Resource;
import java.util.List;

import static java.util.concurrent.TimeUnit.SECONDS;

/**
 * Created by IntelliJ IDEA.
 * User: celestino
 * Date: 6/2/11
 * Time: 4:17 PM
 */
public class TaskInitializer extends BaseStartupService {
    private static final Logger logger = Logger.getLogger(TaskInitializer.class);
    @Resource TaskManager taskManager;
    @Resource List<TaskDescriptor> taskDescriptors;

    @Override
    public void execute() {
        logger.info("scheduling tasks for execution");
        for (TaskDescriptor taskDescriptor : taskDescriptors) {
            taskManager.scheduleTaskAtFixedRate(taskDescriptor.getTask(), taskDescriptor.getInitialDelay(), taskDescriptor.getPeriod(), SECONDS);
        }

    }
}
