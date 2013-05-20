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

package com.ejisto.modules.gui.components.tree.node;

import static com.ejisto.util.GuiUtils.getMessage;

/**
 * Created by IntelliJ IDEA.
 * User: celestino
 * Date: 5/13/13
 * Time: 8:02 AM
 */
public class RootNode extends ClassNode {

    public RootNode() {
        super(null);
    }

    @Override
    public boolean isRoot() {
        return true;
    }

    @Override
    public boolean isLeaf() {
        return false;
    }

    @Override
    public String toString() {
        if (isEmpty()) {
            return getMessage("main.propertieseditor.tree.novalues.text");
        }
        return getMessage("wizard.properties.editor.tab.hierarchical.rootnode");
    }
}
