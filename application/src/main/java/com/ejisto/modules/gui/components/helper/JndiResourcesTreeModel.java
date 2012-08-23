/*
 * Ejisto, a powerful developer assistant
 *
 * Copyright (C) 2010-2011  Celestino Bellone
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

import com.ejisto.modules.dao.entities.JndiDataSource;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import java.util.List;

public class JndiResourcesTreeModel extends DefaultTreeModel {
    private static final long serialVersionUID = -240915564140399391L;
    private List<JndiDataSource> entries;

    public JndiResourcesTreeModel(List<JndiDataSource> entries) {
        super(new DefaultMutableTreeNode("resources"));
        this.entries = entries;
        buildTree();
    }

    public void reload(int index) {
        super.reload((TreeNode) getChild(getRoot(), index));
    }

    public void buildTree() {
        DefaultMutableTreeNode root = (DefaultMutableTreeNode) getRoot();
        root.removeAllChildren();
        for (JndiDataSource entry : entries) {
            root.add(new JndiResourceNode(entry));
        }
    }

    public TreePath getPathForChild(int index) {
        return new TreePath(new Object[]{getRoot(), getChild(getRoot(), index)});
    }

    public static class JndiResourceNode extends DefaultMutableTreeNode {
        private static final long serialVersionUID = -6456522775751905433L;
        private JndiDataSource element;

        public JndiResourceNode(JndiDataSource element) {
            super(element);
            this.element = element;
            refreshList();
        }

        @Override
        public int getIndex(TreeNode node) {
            return children.indexOf(node);
        }

        @Override
        public boolean getAllowsChildren() {
            return true;
        }

        @Override
        public boolean isLeaf() {
            return false;
        }

        public void refreshList() {
            removeAllChildren();
            add(new DefaultMutableTreeNode("type: " + element.getType(), false));
            add(new DefaultMutableTreeNode("jarLocation: " + element.getDriverJarPath(), false));
            add(new DefaultMutableTreeNode("driverClass: " + element.getDriverClassName(), false));
            add(new DefaultMutableTreeNode("url: " + element.getUrl(), false));
            add(new DefaultMutableTreeNode("username: " + element.getUsername(), false));
            add(new DefaultMutableTreeNode("password: " + element.getPassword(), false));
            add(new DefaultMutableTreeNode("maxActive: " + element.getMaxActive(), false));
            add(new DefaultMutableTreeNode("maxIdle: " + element.getMaxIdle(), false));
            add(new DefaultMutableTreeNode("maxWait: " + element.getMaxWait(), false));
        }
    }
}