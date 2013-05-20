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

/**
 * Created by IntelliJ IDEA.
 * User: celestino
 * Date: 2/7/12
 * Time: 8:55 AM
 */
public class FillStrategies {

    public static NodeFillStrategy bestStrategyFor(ClassNode root) {
        return root.isEmpty() ? new EmptyRootNodeFillStrategy() : new InspectionBasedNodeFillStrategy();
    }

    public static FieldNode applyStrategy(NodeFillStrategy strategy, ClassNode root, MockedField field) {
        if (!field.isActive()) {//field deleted
            return strategy.removeField(root, field);
        } else {
            return strategy.insertField(root, field);
        }
    }

    public static FieldNode findChild(ClassNode root, MockedField child) {
        return findChild(bestStrategyFor(root), root, child);
    }

    private static FieldNode findChild(NodeFillStrategy strategy, ClassNode root, MockedField child) {
        return strategy.insertField(root, child);
    }

}
