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
import com.ejisto.util.IteratorEnumeration;
import org.springframework.util.Assert;

import javax.swing.text.Position;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.MutableTreeNode;
import java.util.Enumeration;
import java.util.Map;
import java.util.TreeMap;

import static com.ejisto.util.GuiUtils.encodeTreePath;
import static com.ejisto.util.GuiUtils.getMessage;

/**
 * Created by IntelliJ IDEA.
 * User: celestino
 * Date: 1/23/12
 * Time: 8:26 AM
 */
public class MockedFieldNode extends DefaultMutableTreeNode {
    private static final long serialVersionUID = 1L;
    private MockedField field;
    private boolean head;
    private boolean root;
    private final Map<String, MockedFieldNode> children;
    private String[] nodePath;

    public MockedFieldNode(boolean root) {
        this(null);
        this.root = root;
    }

    public MockedFieldNode(MockedField userObject) {
        this(userObject, false);
    }

    public MockedFieldNode(MockedField userObject, boolean head) {
        super(userObject);
        this.field = userObject;
        this.head = head;
        this.children = new TreeMap<String, MockedFieldNode>();
    }

    @Override
    public String toString() {
        if (root && isEmpty()) {
            return getMessage("main.propertieseditor.tree.novalues.text");
        }
        if (root) {
            return getMessage("wizard.properties.editor.tab.hierarchical.rootnode");
        }
        return nodePath[nodePath.length - 1];
    }

    @Override
    public MockedField getUserObject() {
        return field;
    }

    public boolean isHead() {
        return head;
    }

    public boolean isRoot() {
        return root;
    }

    @Override
    public boolean isLeaf() {
        return !isHead() && !isRoot();
    }

    @Override
    public void add(MutableTreeNode newChild) {
        if (!(newChild instanceof MockedFieldNode)) {
            throw new IllegalArgumentException();
        }
        MockedFieldNode node = (MockedFieldNode) newChild;
        Assert.state(!containsChild(node));
        Assert.isTrue(isParentOf(node), "This node allows only leaf or direct subpackages.");
        children.put(getPathFor(node), node);
        super.add(node);
    }

    public void remove(MockedField child) {
        String path = encodeTreePath(child.getPath());
        Assert.isTrue(children.containsKey(path));
        super.remove(children.remove(path));
    }

    @Override
    public Enumeration<MockedFieldNode> children() {
        return new IteratorEnumeration<MockedFieldNode>(children.values().iterator());
    }

    public boolean containsChild(MockedFieldNode child) {
        return children.containsKey(getPathFor(child));
    }

    public boolean isParentOf(MockedFieldNode node) {
        int depthDifference = 0;
        if (!isRoot()) {
            depthDifference = getNodePath().length - node.getNodePath().length;
        }
        return isRoot() || depthDifference < 0;
    }

    public boolean isEmpty() {
        return children.isEmpty();
    }

    public String[] getNodePath() {
        return nodePath.clone();
    }

    public void setNodePath(String[] nodePath) {
        this.nodePath = nodePath.clone();
    }

    public MockedFieldNode findMatchingNode(String expr, Position.Bias bias) {
        if (bias == Position.Bias.Forward) {
            return findMatchingChild(expr);
        }
        return findMatchingParent(expr);
    }

    private MockedFieldNode findMatchingParent(String expr) {
        MockedFieldNode parent = ((MockedFieldNode) getParent());
        if (parent == null) {
            return null;
        }
        return parent.findMatchingParent(expr);
    }

    private MockedFieldNode findMatchingChild(String expr) {
        for (Map.Entry<String, MockedFieldNode> entry : children.entrySet()) {
            if (entry.getKey().contains(expr)) {
                return entry.getValue();
            }
        }
        for (MockedFieldNode child : children.values()) {
            MockedFieldNode match = child.findMatchingChild(expr);
            if (match != null) {
                return match;
            }
        }
        return null;
    }

    private String getPathFor(MockedFieldNode node) {
        return encodeTreePath(node.getNodePath());
    }
}
