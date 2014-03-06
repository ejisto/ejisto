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
import com.ejisto.event.def.BlockingTaskProgress;
import com.ejisto.modules.controller.DialogController;
import com.ejisto.modules.executor.BackgroundTask;
import com.ejisto.modules.executor.TaskManager;
import com.ejisto.modules.gui.Application;
import com.ejisto.modules.gui.components.ProgressPanel;
import lombok.extern.log4j.Log4j;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicReference;

import static com.ejisto.util.GuiUtils.getMessage;

/**
 * Created by IntelliJ IDEA.
 * User: celestino
 * Date: 8/6/12
 * Time: 6:32 PM
 */
@Log4j
public class TaskProgressNotifier implements ApplicationListener<BlockingTaskProgress> {

    private final Application application;
    private final TaskManager taskManager;
    private final List<String> blacklist = new CopyOnWriteArrayList<>();
    private final ConcurrentMap<String, DialogController> activeControllers = new ConcurrentHashMap<>();
    private final AtomicReference<DialogController> currentController = new AtomicReference<>();

    public TaskProgressNotifier(Application application, TaskManager taskManager) {
        this.application = application;
        this.taskManager = taskManager;
    }

    @Override
    public void onApplicationEvent(final BlockingTaskProgress event) {
        if(blacklist.contains(event.getId())) {
            return;
        }
        if (event.isRunning()) {
            taskManager.addNewTask(new BackgroundTask<>(() -> {
                DialogController controller = DialogController.Builder.newInstance().
                        withContent(buildPanel()).
                        withParentFrame(application).
                        withDecorations(false).
                        withIconKey(event.getIconKey()).
                        withHeader(getMessage(event.getPanelTitle()), getMessage(event.getPanelDescription())).
                        build();
                if(!blacklist.contains(event.getId())) {
                    activeControllers.put(event.getId(), controller);
                    while (activeControllers.containsKey(event.getId()) &&
                            !currentController.compareAndSet(null, controller)) {
                        Thread.sleep(100L);
                    }
                    controller.showUndecorated(true);
                }
                return null;
            }));
        } else {
            closeActiveProgress(event);
        }
    }

    @Override
    public Class<BlockingTaskProgress> getTargetEventType() {
        return BlockingTaskProgress.class;
    }

    private void closeActiveProgress(BlockingTaskProgress event) {
        blacklist.add(event.getId());
        try {
            log.debug("trying to close active progress for event id: " + event.getId());
            DialogController controller = activeControllers.get(event.getId());
            log.debug("found controller: " + controller);
            int count = 0;
            while (count++ < 50 && (controller == null || controller != currentController.get())) {
                log.debug("sleeping 50ms...");
                Thread.sleep(50L);
                controller = activeControllers.get(event.getId());
            }
            if(controller != null) {
                currentController.compareAndSet(controller, null);
                activeControllers.remove(event.getId());
                log.debug("hiding controller");
                controller.hide();
                log.debug("hidden");
            } else {
                log.debug("controller not found...");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("got InterruptedException: ", e);
        }
    }

    private JPanel buildPanel() {
        return new ProgressPanel();
    }
}
