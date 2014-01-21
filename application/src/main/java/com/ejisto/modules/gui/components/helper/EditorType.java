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

package com.ejisto.modules.gui.components.helper;

import java.util.Arrays;
import java.util.Optional;

import static com.ejisto.util.GuiUtils.getMessage;

/**
 * Created by IntelliJ IDEA.
 * User: celestino
 * Date: 9/18/11
 * Time: 5:22 PM
 */
public enum EditorType {
    HIERARCHICAL(getMessage("wizard.properties.editor.tab.hierarchical.text"), 0),
    FLATTEN(getMessage("wizard.properties.editor.tab.flat.text"), 1);
    private final String label;
    private final int index;

    private EditorType(String label, int index) {
        this.label = label;
        this.index = index;
    }

    public String getLabel() {
        return label;
    }

    public int getIndex() {
        return index;
    }

    @Override
    public String toString() {
        return label;
    }

    public static Optional<EditorType> fromIndex(final int index) {
        return Arrays.stream(values())
                .filter(v -> v.index == index)
                .findFirst();
    }
}
