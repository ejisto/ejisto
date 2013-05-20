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

package com.ejisto.modules.gui.components.tree;

import com.ejisto.modules.dao.entities.MockedField;
import com.ejisto.modules.gui.components.tree.node.ClassNode;
import com.ejisto.modules.gui.components.tree.node.FieldNode;

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

    private final Map<String, ClassNode> map;

    public EmptyRootNodeFillStrategy() {
        this.map = new TreeMap<>();
    }

    @Override
    public FieldNode insertField(ClassNode parent, MockedField field) {
        ClassNode parentNode = createParents(field, parent, map);
        FieldNode childNode = new FieldNode(field);
        parentNode.add(childNode);
        return childNode;
    }

    @Override
    public FieldNode removeField(ClassNode parent, MockedField field) {
        throw new UnsupportedOperationException("not supported");
    }

    @Override
    public boolean containsChild(ClassNode parent, MockedField child) {
        return map.containsKey(child.getParentClassPathAsString());
    }

    private ClassNode createParents(MockedField field, ClassNode parent, Map<String, ClassNode> mapping) {
        String firstPath = encodeTreePath(field.getParentClassPath(), 0, 1);
        ClassNode first = mapping.get(firstPath);
        boolean firstExists = first != null;
        if (!firstExists) {
            first = new ClassNode(field, new String[]{firstPath});
            mapping.put(firstPath, first);
            parent.add(first);
        }
        return createAncestors(field, first, 0, mapping);
    }

    private ClassNode createAncestors(MockedField field, ClassNode parent, int depth, Map<String, ClassNode> mapping) {
        String[] originalPath = field.getPath();
        int currentDepth = depth + 1;
        String[] childPath = getParentPath(currentDepth, originalPath);
        String childPathAsString = encodeTreePath(childPath);
        ClassNode child = mapping.get(childPathAsString);
        boolean childExists = child != null;
        if (!childExists) {
            child = new ClassNode(field, childPath);
            mapping.put(childPathAsString, child);
            parent.add(child);
        }
        if ((currentDepth + 2) < originalPath.length) {
            return createAncestors(field, child, currentDepth, mapping);
        }
        return child;
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
