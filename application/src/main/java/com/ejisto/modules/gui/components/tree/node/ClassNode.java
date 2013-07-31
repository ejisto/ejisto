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

import javax.swing.text.Position;
import javax.swing.tree.MutableTreeNode;
import java.util.*;

import static com.ejisto.util.GuiUtils.encodeTreePath;

/**
 * Created by IntelliJ IDEA.
 * User: celestino
 * Date: 5/13/13
 * Time: 8:14 AM
 */
@Log4j
public class ClassNode extends FieldNode {

    private final Map<String, FieldNode> children;
    private final String[] path;

    public ClassNode(MockedField userObject) {
        this(userObject, userObject != null ? userObject.getPath() : new String[0]);
    }

    public ClassNode(MockedField userObject, String[] path) {
        super(userObject);
        Objects.requireNonNull(path);
        this.children = new TreeMap<>();
        this.path = Arrays.copyOf(path, path.length);
    }

    @Override
    public String toString() {
        if (path.length == 0) {
            return "";
        }
        return path[path.length - 1];
    }

    @Override
    public String[] getNodePath() {
        return path.clone();
    }

    @Override
    public boolean isLeaf() {
        return false;
    }

    @Override
    public boolean isRoot() {
        return false;
    }

    @Override
    public void add(MutableTreeNode newChild) {
        if (!(newChild instanceof FieldNode)) {
            throw new IllegalArgumentException();
        }
        FieldNode node = (FieldNode) newChild;
        if (containsChild(node)) {
            log.debug("node already contains child named " + node);
            return;
        }
        if (!isParentOf(node)) {
            throw new IllegalArgumentException(
                    String.format("This node (%s) allows only leaf or direct subpackages. No room for %s",
                                  Arrays.toString(getNodePath()), Arrays.toString(node.getNodePath())));
        }
        children.put(getPathFor(node), node);
        super.add(node);
    }

    public void remove(MockedField child) {
        String path = encodeTreePath(child.getPath());
        if (!children.containsKey(path)) {
            throw new IllegalArgumentException();
        }
        super.remove(children.remove(path));
    }

    @Override
    @SuppressWarnings("unchecked")
    public Enumeration<FieldNode> children() {
        return super.children();
    }

    public boolean containsChild(FieldNode child) {
        return children.containsKey(getPathFor(child));
    }

    public boolean isParentOf(FieldNode node) {
        int depthDifference = 0;
        if (!isRoot()) {
            depthDifference = getNodePath().length - node.getNodePath().length;
        }
        return isRoot() || depthDifference < 0;
    }

    @Override
    public boolean isEmpty() {
        return children.isEmpty();
    }

    public FieldNode findMatchingNode(String expr, Position.Bias bias) {
        if (bias == Position.Bias.Forward) {
            return findMatchingChild(expr);
        }
        return findMatchingParent(expr);
    }

    private FieldNode findMatchingParent(String expr) {
        ClassNode parent = ((ClassNode) getParent());
        if (parent == null) {
            return null;
        }
        return parent.findMatchingParent(expr);
    }

    private FieldNode findMatchingChild(String expr) {
        for (Map.Entry<String, FieldNode> entry : children.entrySet()) {
            if (entry.getKey().contains(expr)) {
                return entry.getValue();
            }
        }
        for (FieldNode child : children.values()) {
            FieldNode match = null;
            if (child instanceof ClassNode) {
                match = ((ClassNode) child).findMatchingChild(expr);
            }
            if (match != null) {
                return match;
            }
        }
        return null;
    }

    private String getPathFor(FieldNode node) {
        return encodeTreePath(node.getNodePath());
    }
}
