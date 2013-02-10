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

/**
 * Created by IntelliJ IDEA.
 * User: celestino
 * Date: 1/30/12
 * Time: 6:39 PM
 */
public interface NodeFillStrategy {
    /**
     * Inserts a child using its path.<br/>
     * This method creates parent nodes if they don't exist
     *
     * @param parent The parent node
     * @param field  child field
     * @return the parent node of the inserted node. Target node will be created if does not exists.
     */
    MockedFieldNode insertField(MockedFieldNode parent, MockedField field);

    /**
     * Removes a child from parent.<br/>
     *
     * @param parent The parent node
     * @param field  child field
     * @return the parent node of the removed node.
     */
    MockedFieldNode removeField(MockedFieldNode parent, MockedField field);

    /**
     * Checks if given node contains a child identified by childPath
     *
     * @param parent The parent node
     * @param child  the child to insertField
     * @return @{code true} if found, @{false} otherwise.
     */
    boolean containsChild(MockedFieldNode parent, MockedField child);


}
