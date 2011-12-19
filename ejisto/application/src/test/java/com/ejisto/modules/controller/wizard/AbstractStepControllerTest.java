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

package com.ejisto.modules.controller.wizard;

import com.ejisto.modules.controller.wizard.installer.ApplicationScanningController;
import com.ejisto.modules.executor.GuiTask;
import com.ejisto.modules.executor.Task;
import org.junit.Before;
import org.junit.Test;

import java.beans.PropertyChangeEvent;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static javax.swing.SwingWorker.StateValue.DONE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * Created by IntelliJ IDEA.
 * User: celestino
 * Date: 12/12/11
 * Time: 8:54 AM
 */
public class AbstractStepControllerTest {
    ApplicationScanningController applicationScanningController;

    @Before
    public void init() {
        applicationScanningController = new ApplicationScanningController(null, null) {
            @Override
            protected Task<?> createNewTask() {
                return new GuiTask<Object>(new Callable<Object>() {
                    @Override
                    public Object call() throws Exception {
                        Thread.sleep(1000L);
                        applicationScanningController.propertyChange(
                                new PropertyChangeEvent(this, "state", null, DONE));
                        return null;
                    }
                }, "test");
            }
        };
    }

    @Test
    public void testExecutionCompleted() {
        ExecutorService executorService = Executors.newCachedThreadPool();
        final CyclicBarrier barrier = new CyclicBarrier(101);
        final AtomicInteger counter = new AtomicInteger();
        for (int i = 0; i < 100; i++)
            executorService.submit(new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    try {
                        applicationScanningController.executionCompleted();
                    } catch (Exception ex) {
                        counter.incrementAndGet();
                    }
                    barrier.await();
                    return null;
                }
            });
        try {
            barrier.await(1200, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            fail(e.toString());
        }

        assertEquals(99, counter.get());

    }
}
