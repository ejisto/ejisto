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

package com.ejisto.modules.gui.components;

import org.jdesktop.swingx.JXPanel;

import java.awt.*;

/**
 * Created by IntelliJ IDEA.
 * User: celestino
 * Date: 3/6/11
 * Time: 1:03 PM
 */
public class ProgressWithHeader extends JXPanel {

    private String containerDescription;
    private String title;
    private Header header;
    private ProgressPanel progressPanel;
    private String iconKey;

    public ProgressWithHeader(String title, String containerDescription, String iconKey) {
        this.title = title;
        this.containerDescription = containerDescription;
        this.iconKey = iconKey;
        init();
    }

    public void notifyJobCompleted(String nextJobDescription) {
        getProgressPanel().jobCompleted(nextJobDescription);
    }

    public void initProgress(int jobs, String text) {
        getProgressPanel().initProgress(jobs, text);
    }

    private void init() {
        setLayout(new BorderLayout());
        add(getHeader(), BorderLayout.NORTH);
        add(getProgressPanel(), BorderLayout.CENTER);
    }

    private Header getHeader() {
        if (this.header != null) {
            return header;
        }
        header = new Header(title, containerDescription, iconKey);
        return header;
    }

    private ProgressPanel getProgressPanel() {
        if (this.progressPanel != null) {
            return progressPanel;
        }
        progressPanel = new ProgressPanel();
        return progressPanel;
    }

}
