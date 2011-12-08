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

package com.ejisto.modules.gui.components;

import org.jdesktop.swingx.JXLabel;
import org.jdesktop.swingx.JXPanel;

import javax.swing.*;
import java.awt.*;

import static com.ejisto.util.GuiUtils.getMessage;

public class ProgressPanel extends JXPanel {

    private static final long serialVersionUID = -2033285248036487856L;
    private String defaultMessage;
    private JProgressBar progress = null;
    private JXLabel title = null;
    private int jobsCompleted = 0;

    /**
     * This method initializes
     */
    public ProgressPanel() {
        super();
        initialize();
    }

    /**
     * This method initializes this
     */
    private void initialize() {
        this.defaultMessage = getMessage("progress.start");
        this.title = new JXLabel(defaultMessage, JXLabel.CENTER);
        this.title.setLineWrap(true);
        this.setLayout(new BorderLayout());
        this.setPreferredSize(new Dimension(500, 300));
        this.setMinimumSize(new Dimension(500, 300));
        this.setSize(new Dimension(500, 217));
        this.add(getProgress(), BorderLayout.SOUTH);
        this.add(title, BorderLayout.CENTER);
    }

    /**
     * This method initializes progress
     *
     * @return javax.swing.JProgressBar
     */
    private JProgressBar getProgress() {
        if (progress == null) {
            progress = new JProgressBar();
            progress.setIndeterminate(true);
        }
        return progress;
    }

    public void initProgress(int jobs, String text) {
        jobsCompleted = 0;
        title.setText(text);
        progress.setMaximum(jobs);
        progress.setValue(0);
        progress.setIndeterminate(false);
    }

    public void jobCompleted(String newText) {
        if (!progress.isIndeterminate()) progress.setValue(++jobsCompleted);
        if (newText != null) title.setText(newText);
    }

    public void jobCompleted() {
        jobCompleted(null);
    }

    public void reset() {
        getProgress().setIndeterminate(true);
        title.setText(defaultMessage);
    }

    public void processCompleted(String text) {
        progress.setIndeterminate(false);
        progress.setMaximum(1);
        progress.setMinimum(0);
        jobCompleted(text);
    }

}  //  @jve:decl-index=0:visual-constraint="10,10"
