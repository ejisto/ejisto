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

import ch.lambdaj.group.Group;
import com.ejisto.modules.dao.entities.MockedField;

import javax.swing.tree.TreeNode;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static ch.lambdaj.Lambda.*;
import static com.ejisto.modules.gui.components.helper.FillStrategies.bestStrategyFor;
import static com.ejisto.util.GuiUtils.encodeTreePath;

/**
 * Created by IntelliJ IDEA.
 * User: celestino
 * Date: 1/23/12
 * Time: 8:29 AM
 */
public class MockedFieldTreeElementsBuilder {

    public static void fillRootNode(MockedFieldNode root, List<MockedField> fields) {
        NodeFillStrategy strategy = bestStrategyFor(root);
        for (MockedField field : fields) {
            strategy.insertField(root, field);
        }
    }

//    public static void fillRootNode(MockedFieldNode root, List<MockedField> fields) {
//        Map<String, MockedFieldNode> mapping = new TreeMap<String, MockedFieldNode>();
//        for (MockedField field : fields) {
//            createParents(field, root, mapping);
//        }
//    }

    private static void createParents(MockedField field, MockedFieldNode root, Map<String, MockedFieldNode> mapping) {
        String firstPath = encodeTreePath(field.getParentClassPath(), 0, 1);
        MockedFieldNode first = mapping.get(firstPath);
        if (first == null) {
            first = new MockedFieldNode(field, true);
            first.setNodePath(new String[]{field.getParentClassPath()[0]});
            mapping.put(firstPath, first);
            root.add(first);
        }
        createParents(field, first, 0, mapping);
    }

    private static void createParents(MockedField field, MockedFieldNode parent, int depth, Map<String, MockedFieldNode> mapping) {
        String[] originalPath = field.getParentClassPath();
        String[] childPath = getParentPath(depth + 1, field.getParentClassPath());
        String childPathAsString = encodeTreePath(childPath);
        if (depth == originalPath.length - 1) {
            parent.add(new MockedFieldNode(field));
            return;
        }
        MockedFieldNode child = mapping.get(childPathAsString);
        if (child == null) {
            child = new MockedFieldNode(field, true);
            mapping.put(childPathAsString, child);
            String[] nodePath = new String[depth + 1];
            System.arraycopy(childPath, 0, nodePath, 0, nodePath.length);
            child.setNodePath(childPath);
            parent.add(child);
        }

        if (depth < originalPath.length - 1) createParents(field, child, depth + 1, mapping);
    }

    private static String[] getParentPath(int depth, String[] originalPath) {
        if (depth == originalPath.length) return originalPath;
        String[] newPath = new String[depth + 1];
        System.arraycopy(originalPath, 0, newPath, 0, depth + 1);
        return newPath;
    }

    /**
     * Utility method that converts a <b>sorted</b> Collection of MockedField in
     * a List of TreeNode
     *
     * @param in Collection of MockedFields
     * @return Tree structure
     */
    private TreeNode mockedFields2Nodes(Collection<MockedField> in) {
        if (in.isEmpty()) return new EmptyTreeNode();
        Group<MockedField> groupedFields = group(in, by(on(MockedField.class).getContextPath()), by(
                on(MockedField.class).getPackageName()),
                                                 by(on(MockedField.class).getClassSimpleName()),
                                                 by(on(MockedField.class).getFieldName()));
        return group2Node(groupedFields, 0);
    }

    private MockedFieldNode group2Node(Group<MockedField> group, int depth) {
        MockedFieldNode node;
        if (group.isLeaf()) {
            node = new MockedFieldNode(group.first());
        } else {
            node = new MockedFieldNode(group.first(), depth < 2);
            for (Group<MockedField> child : group.subgroups())
                node.add(group2Node(child, depth + 1));
        }
        return node;
    }

    public static final class EmptyTreeNode extends MockedFieldNode {
        private static final long serialVersionUID = 1L;

        public EmptyTreeNode() {
            super(true);
        }
    }
}
