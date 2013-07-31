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

import com.ejisto.modules.dao.entities.MockedField;
import lombok.extern.log4j.Log4j;

import javax.swing.tree.DefaultMutableTreeNode;
import java.util.Arrays;

/**
 * Created by IntelliJ IDEA.
 * User: celestino
 * Date: 1/23/12
 * Time: 8:26 AM
 */
@Log4j
public class FieldNode extends DefaultMutableTreeNode {
    private static final long serialVersionUID = 1L;
    private MockedField field;
    private final String[] nodePath;

    public FieldNode(MockedField userObject) {
        super(userObject);
        this.field = userObject;
        String[] objectPath = userObject != null ? userObject.getPath() : new String[0];
        this.nodePath = Arrays.copyOf(objectPath, objectPath.length);
    }

    @Override
    public String toString() {
        return nodePath[nodePath.length - 1];
    }

    @Override
    public MockedField getUserObject() {
        return field;
    }

    public boolean isRoot() {
        return false;
    }

    @Override
    public boolean isLeaf() {
        return true;
    }

    public String[] getNodePath() {
        return nodePath;
    }

    public boolean isEmpty() {
        return true;
    }
}
