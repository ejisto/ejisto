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
import com.ejisto.modules.gui.components.tree.EmptyRootNodeFillStrategy;
import com.ejisto.modules.gui.components.tree.node.ClassNode;
import com.ejisto.modules.gui.components.tree.node.FieldNode;
import com.ejisto.modules.gui.components.tree.NodeFillStrategy;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

/**
 * Created by IntelliJ IDEA.
 * User: celestino
 * Date: 1/31/12
 * Time: 8:46 AM
 */
public class EmptyRootNodeFillStrategyTest {

    @Mock private ClassNode root;
    @Mock private MockedField field1;
    @Mock private MockedField field2;
    @Mock private MockedField field3;
    @Mock private MockedField field4;


    @Before
    public void initMocks() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testFillAndSearch() throws Exception {
        when(field1.getParentClassPath()).thenReturn(
                new String[]{"/ejisto-test", "this", "is", "a", "test", "TestClass"});
        when(field1.getPath()).thenReturn(
                new String[]{"/ejisto-test", "this", "is", "a", "test", "TestClass", "testProperty"});
        when(field1.getParentClassPathAsString()).thenReturn("/ejisto-test>this>is>a>test>TestClass");
        NodeFillStrategy strategy = new EmptyRootNodeFillStrategy();
        FieldNode node = strategy.insertField(root, field1);
        verify(root, times(1)).add(any(FieldNode.class));
        assertNotNull(node);
        assertNotSame(root, node);
        assertTrue(strategy.containsChild(root, field1));
    }

    @Test
    public void testNodeNotFound() {
        NodeFillStrategy strategy = new EmptyRootNodeFillStrategy();
        when(field1.getParentClassPath()).thenReturn(
                new String[]{"/ejisto-test", "this", "is", "a", "test", "TestClass"});
        when(field1.getParentClassPathAsString()).thenReturn("/ejisto-test>this>is>a>test>TestClass");
        assertFalse(strategy.containsChild(root, field1));
    }
}
