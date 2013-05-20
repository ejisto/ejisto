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

import java.util.Enumeration;
import java.util.Objects;

import static com.ejisto.util.GuiUtils.encodeTreePath;

/**
 * Created by IntelliJ IDEA.
 * User: celestino
 * Date: 1/30/12
 * Time: 6:48 PM
 */
public class InspectionBasedNodeFillStrategy implements NodeFillStrategy {

    public InspectionBasedNodeFillStrategy() {
    }

    @Override
    public FieldNode insertField(ClassNode parent, MockedField child) {
        NodeOperationHelper operationHelper = new NodeOperationHelper();
        operationHelper.whenFound = new WhenFoundAction() {
            @Override
            FieldNode doAction(ClassNode parent, MockedField child) {
                return insertNode(parent, child);
            }
        };
        operationHelper.whenNotFound = new WhenNotFoundAction() {
            @Override
            FieldNode doAction(ClassNode parent, MockedField child, int depthDifference) {
                return createParent(parent, child, depthDifference);
            }
        };
        return handleNodeModification(parent, child, operationHelper);
    }

    @Override
    public FieldNode removeField(ClassNode parent, MockedField child) {
        NodeOperationHelper operationHelper = new NodeOperationHelper();
        operationHelper.whenFound = new WhenFoundAction() {
            @Override
            FieldNode doAction(ClassNode parent, MockedField child) {
                return deleteNode(parent, child);
            }
        };
        return handleNodeModification(parent, child, operationHelper);
    }

    private FieldNode handleNodeModification(ClassNode parent, MockedField child, NodeOperationHelper operationHelper) {
        if (!containsChild(parent, child)) {
            throw new IllegalArgumentException("child not compatible.");
        }
        int depthDifference = calcDepthDifference(parent, child);
        if(depthDifference > 0) {
            throw new IllegalArgumentException("child not compatible.");
        }
        if (depthDifference == 0) {
            return operationHelper.whenFound.doAction(parent, child);
        }
        Enumeration<FieldNode> en = parent.children();
        while (en.hasMoreElements()) {
            FieldNode candidate = en.nextElement();
            if(candidate instanceof ClassNode) {
                if (containsChild((ClassNode)candidate, child)) {
                    return handleNodeModification((ClassNode)candidate, child, operationHelper);
                }
            }
        }
        if (operationHelper.whenNotFound != null) {
            return operationHelper.whenNotFound.doAction(parent, child, depthDifference);
        }
        return parent;
    }

    FieldNode insertNode(ClassNode parent, MockedField child) {
        FieldNode node = new FieldNode(child);
        if (!parent.containsChild(node)) {
            parent.add(node);
        }
        return node;
    }

    FieldNode deleteNode(ClassNode parent, MockedField child) {
        parent.remove(child);
        return null;
    }

    private int calcDepthDifference(FieldNode parent, MockedField child) {
        if (parent.isRoot()) {
            return -(child.getParentClassPath().length);
        }
        return parent.getNodePath().length - child.getParentClassPath().length;
    }

    private FieldNode createParent(ClassNode root, MockedField child, int depthDifference) {
        if (depthDifference < 0) {
            String[] childPath = child.getParentClassPath();
            String[] nodePath = new String[depthDifference + childPath.length + 1];
            System.arraycopy(childPath, 0, nodePath, 0, nodePath.length);
            ClassNode parent = new ClassNode(child, nodePath);
            root.add(parent);
            return createParent(parent, child, ++depthDifference);
        }
        FieldNode node = new FieldNode(child);
        root.add(node);
        return node;
    }

    @Override
    public boolean containsChild(ClassNode parent, MockedField child) {
        Objects.requireNonNull(parent, "parent can't be null");
        Objects.requireNonNull(child, "child can't be null");
        if (!parent.isRoot()) {
            Objects.requireNonNull(parent.getUserObject(), "parent.userObject can't be null");
        }
        return parent.isRoot() || child.getParentClassPathAsString().startsWith(encodeTreePath(parent.getNodePath()));
    }

    private class NodeOperationHelper {
        private WhenFoundAction whenFound;
        private WhenNotFoundAction whenNotFound;
    }

    private abstract class WhenFoundAction {
        abstract FieldNode doAction(ClassNode parent, MockedField child);
    }

    private abstract class WhenNotFoundAction {
        abstract FieldNode doAction(ClassNode parent, MockedField child, int depthDifference);
    }
}
