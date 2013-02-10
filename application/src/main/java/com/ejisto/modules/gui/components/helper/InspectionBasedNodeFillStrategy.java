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

import com.ejisto.modules.dao.entities.MockedField;
import org.springframework.util.Assert;

import java.util.Enumeration;

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
    public MockedFieldNode insertField(MockedFieldNode parent, MockedField child) {
        NodeOperationHelper operationHelper = new NodeOperationHelper();
        operationHelper.whenFound = new WhenFoundAction() {
            @Override
            void doAction(MockedFieldNode parent, MockedField child) {
                insertNode(parent, child);
            }
        };
        operationHelper.whenNotFound = new WhenNotFoundAction() {
            @Override
            void doAction(MockedFieldNode parent, MockedField child, int depthDifference) {
                createParent(parent, child, depthDifference);
            }
        };
        return handleNodeModification(parent, child, operationHelper);
    }

    @Override
    public MockedFieldNode removeField(MockedFieldNode parent, MockedField child) {
        NodeOperationHelper operationHelper = new NodeOperationHelper();
        operationHelper.whenFound = new WhenFoundAction() {
            @Override
            void doAction(MockedFieldNode parent, MockedField child) {
                deleteNode(parent, child);
            }
        };
        return handleNodeModification(parent, child, operationHelper);
    }

    private MockedFieldNode handleNodeModification(MockedFieldNode parent, MockedField child, NodeOperationHelper operationHelper) {
        if (!containsChild(parent, child)) {
            throw new IllegalArgumentException("child not compatible.");
        }
        int depthDifference = calcDepthDifference(parent, child);
        Assert.isTrue(depthDifference <= 0);
        if (depthDifference == 0) {
            operationHelper.whenFound.doAction(parent, child);
            return parent;
        }
        Enumeration<MockedFieldNode> en = parent.children();
        while (en.hasMoreElements()) {
            MockedFieldNode candidate = en.nextElement();
            if (containsChild(candidate, child)) {
                return handleNodeModification(candidate, child, operationHelper);
            }
        }
        if (operationHelper.whenNotFound != null) {
            operationHelper.whenNotFound.doAction(parent, child, depthDifference);
        }
        return parent;
    }

    void insertNode(MockedFieldNode parent, MockedField child) {
        MockedFieldNode node = new MockedFieldNode(child);
        node.setNodePath(child.getPath());
        if (!parent.containsChild(node)) {
            parent.add(node);
        }
    }

    void deleteNode(MockedFieldNode parent, MockedField child) {
        parent.remove(child);
    }

    private int calcDepthDifference(MockedFieldNode parent, MockedField child) {
        if (parent.isRoot()) {
            return -(child.getParentClassPath().length);
        }
        return parent.getNodePath().length - child.getParentClassPath().length;
    }

    private void createParent(MockedFieldNode root, MockedField child, int depthDifference) {
        if (depthDifference < 0) {
            MockedFieldNode parent = new MockedFieldNode(child, true);
            String[] childPath = child.getParentClassPath();
            String[] nodePath = new String[depthDifference + childPath.length + 1];
            System.arraycopy(childPath, 0, nodePath, 0, nodePath.length);
            parent.setNodePath(nodePath);
            root.add(parent);
            createParent(parent, child, ++depthDifference);
        } else {
            MockedFieldNode node = new MockedFieldNode(child);
            node.setNodePath(child.getPath());
            root.add(node);
        }
    }

    @Override
    public boolean containsChild(MockedFieldNode parent, MockedField child) {
        Assert.notNull(parent, "parent can't be null");
        Assert.notNull(child, "child can't be null");
        if (!parent.isRoot()) {
            Assert.notNull(parent.getUserObject(), "parent.userObject can't be null");
        }
        return parent.isRoot() || child.getParentClassPathAsString().startsWith(encodeTreePath(parent.getNodePath()));
    }


    private class NodeOperationHelper {
        private WhenFoundAction whenFound;
        private WhenNotFoundAction whenNotFound;
    }

    private abstract class WhenFoundAction {
        abstract void doAction(MockedFieldNode parent, MockedField child);
    }

    private abstract class WhenNotFoundAction {
        abstract void doAction(MockedFieldNode parent, MockedField child, int depthDifference);
    }
}
