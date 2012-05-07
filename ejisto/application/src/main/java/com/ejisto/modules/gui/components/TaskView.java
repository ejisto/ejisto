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

package com.ejisto.modules.gui.components;

import com.ejisto.util.GuiUtils;
import org.jdesktop.swingx.JXPanel;

import javax.swing.*;
import java.awt.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by IntelliJ IDEA.
 * User: celestino
 * Date: 4/10/11
 * Time: 4:14 PM
 */
public class TaskView extends JXPanel {
    private boolean minimized;
    private JProgressBar progressBar;
    private AtomicInteger completedTasks = new AtomicInteger(0);
    private AtomicInteger totalTasks = new AtomicInteger(0);

    public TaskView() {
        super();
        initComponents();
    }

    public void setMinimized(boolean minimized) {
        this.minimized = minimized;
    }

    public synchronized void setCurrentStatus(int completedTasks, int totalTasks) {
        this.completedTasks.set(completedTasks);
        this.totalTasks.set(totalTasks);
        updateProgressBar();
    }

    public void taskCompleted() {
        this.completedTasks.incrementAndGet();
    }

    private void initComponents() {
        setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
        setPreferredSize(new Dimension(80, 20));
        setSize(new Dimension(80, 20));
        add(getProgressBar());
    }

    private JProgressBar getProgressBar() {
        if (progressBar != null) {
            return progressBar;
        }
        progressBar = new JProgressBar();
        progressBar.setPreferredSize(new Dimension(80, 20));
        return progressBar;
    }

    private void updateProgressBar() {
        if (getProgressBar().isIndeterminate()) {
            getProgressBar().setIndeterminate(false);
        }
        getProgressBar().setMinimum(0);
        int total = totalTasks.get();
        int completed = completedTasks.get();
        getProgressBar().setMaximum(total);
        getProgressBar().setValue(completed);
        setToolTipText(GuiUtils.getMessage("status.progress.tooltip", total, completed, percent(total, completed)));
    }

    private int percent(int total, int completed) {
        if (total == 0) {
            return 100;
        }
        return (int) ((double) completed / (double) total * 100);
    }
}
