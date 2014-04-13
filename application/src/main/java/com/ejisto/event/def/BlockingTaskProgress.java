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

package com.ejisto.event.def;

/**
 * Created by IntelliJ IDEA.
 * User: celestino
 * Date: 8/6/12
 * Time: 6:26 PM
 */
public class BlockingTaskProgress extends BaseApplicationEvent {

    private final String panelTitle;
    private final String panelDescription;
    private final boolean running;
    private String iconKey;
    private String id;

    public BlockingTaskProgress(Object source, String id, String panelTitle, String panelDescription, String iconKey, boolean running) {
        super(source);
        this.id = id;
        this.panelTitle = panelTitle;
        this.panelDescription = panelDescription;
        this.iconKey = iconKey;
        this.running = running;
    }

    @Override
    public String getDescription() {
        return "progress";
    }

    @Override
    public String getKey() {
        return "progress";
    }

    @Override
    protected String getEventDescriptionValue() {
        return getId();
    }

    public String getPanelTitle() {
        return panelTitle;
    }

    public String getPanelDescription() {
        return panelDescription;
    }

    public boolean isRunning() {
        return running;
    }

    public String getIconKey() {
        return iconKey;
    }

    public String getId() {
        return id;
    }

}
