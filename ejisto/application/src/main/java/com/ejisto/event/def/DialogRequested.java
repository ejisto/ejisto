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

package com.ejisto.event.def;

import com.ejisto.constants.StringConstants;

import java.awt.*;

/**
 * Created by IntelliJ IDEA.
 * User: celestino
 * Date: 1/24/12
 * Time: 7:26 PM
 */
public class DialogRequested extends BaseApplicationEvent {

    public enum DialogType {
        ABOUT("menu.about", 530, 350, false);
        private final String description;
        private final Dimension dimension;
        private final boolean resizable;

        DialogType(String description, int w, int h, boolean resizable) {
            this.description = description;
            this.dimension = new Dimension(w, h);
            this.resizable = resizable;
        }

        public String getDescription() {
            return description;
        }

        public Dimension getDimension() {
            return new Dimension(dimension);
        }

        public boolean isResizable() {
            return resizable;
        }
    }

    private DialogType type;

    public DialogRequested(Object source, DialogType type) {
        super(source);
        this.type = type;
    }

    @Override
    public String getDescription() {
        return type.getDescription();
    }

    @Override
    public String getKey() {
        return StringConstants.SHOW_ABOUT_PANEL.getValue();
    }

    public Dimension getDialogSize() {
        return type.getDimension();
    }

    public boolean isResizable() {
        return type.isResizable();
    }

}
