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

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;

/**
 * Created by IntelliJ IDEA.
 * User: celestino
 * Date: 1/12/12
 * Time: 8:22 AM
 */
public class MockedFieldTreeModel extends DefaultTreeModel {

    private final Map<String, TreePath> treePathsCache;

    public MockedFieldTreeModel() {
        super(new RootNode());
        treePathsCache = new TreeMap<String, TreePath>();
    }

    @Override
    public MockedFieldNode getChild(Object parent, int index) {
        return (MockedFieldNode) super.getChild(parent, index);
    }

    @Override
    public RootNode getRoot() {
        return (RootNode) super.getRoot();
    }

    private static class RootNode extends MockedFieldNode {
        private static final long serialVersionUID = 1L;

        public RootNode() {
            super(null);
        }

        @Override
        public boolean isRoot() {
            return true;
        }

        @SuppressWarnings("unchecked")
        public Collection<MockedFieldNode> getChildren() {
            return new ArrayList<MockedFieldNode>(super.children);
        }
    }

    private static class MockedFieldNode extends DefaultMutableTreeNode {
        private static final long serialVersionUID = 1L;
        private MockedField field;
        private boolean head;

        public MockedFieldNode(MockedField userObject) {
            this(userObject, false);
        }

        public MockedFieldNode(MockedField userObject, boolean head) {
            super(userObject);
            this.field = userObject;
            this.head = head;
        }

        @Override
        public String toString() {
            return head ? field.getPackageName() : field.getClassSimpleName();
        }

        public boolean isRoot() {
            return false;
        }
    }
}
