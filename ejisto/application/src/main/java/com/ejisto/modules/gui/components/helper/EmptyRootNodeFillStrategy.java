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

package com.ejisto.modules.gui.components.helper;

import com.ejisto.modules.dao.entities.MockedField;

import java.util.Map;
import java.util.TreeMap;

import static com.ejisto.util.GuiUtils.encodeTreePath;

/**
 * Created by IntelliJ IDEA.
 * User: celestino
 * Date: 1/30/12
 * Time: 6:47 PM
 */
public class EmptyRootNodeFillStrategy implements NodeFillStrategy {

    private final Map<String, MockedFieldNode> map;

    public EmptyRootNodeFillStrategy() {
        this.map = new TreeMap<String, MockedFieldNode>();
    }

    @Override
    public MockedFieldNode insertField(MockedFieldNode parent, MockedField field) {
        return createParents(field, parent, map);
    }

    @Override
    public MockedFieldNode removeField(MockedFieldNode parent, MockedField field) {
        throw new UnsupportedOperationException("not supported");
    }

    @Override
    public boolean containsChild(MockedFieldNode parent, MockedField child) {
        return map.containsKey(child.getParentClassPathAsString());
    }

    private MockedFieldNode createParents(MockedField field, MockedFieldNode root, Map<String, MockedFieldNode> mapping) {
        String firstPath = encodeTreePath(field.getParentClassPath(), 0, 1);
        MockedFieldNode first = mapping.get(firstPath);
        boolean firstExists = first != null;
        if (!firstExists) {
            first = new MockedFieldNode(field, true);
            first.setNodePath(new String[]{firstPath});
            mapping.put(firstPath, first);
            root.add(first);
        }
        MockedFieldNode last = createParents(field, first, 0, mapping);
        return firstExists ? last : root;
    }

    private MockedFieldNode createParents(MockedField field, MockedFieldNode parent, int depth, Map<String, MockedFieldNode> mapping) {
        String[] originalPath = field.getPath();
        String[] childPath = getParentPath(depth + 1, originalPath);
        String childPathAsString = encodeTreePath(childPath);
        if (depth == originalPath.length - 2) {
            MockedFieldNode node = new MockedFieldNode(field);
            node.setNodePath(field.getPath());
            parent.add(node);
            mapping.put(childPathAsString, node);
            return parent;
        }
        MockedFieldNode child = mapping.get(childPathAsString);
        boolean childExists = child != null;
        if (!childExists) {
            child = new MockedFieldNode(field, true);
            mapping.put(childPathAsString, child);
            child.setNodePath(childPath);
            parent.add(child);
        }
        MockedFieldNode last = createParents(field, child, depth + 1, mapping);
        return childExists ? last : parent;
    }

    private String[] getParentPath(int depth, String[] originalPath) {
        if (depth == originalPath.length) {
            return originalPath;
        }
        String[] newPath = new String[depth + 1];
        System.arraycopy(originalPath, 0, newPath, 0, depth + 1);
        return newPath;
    }
}
