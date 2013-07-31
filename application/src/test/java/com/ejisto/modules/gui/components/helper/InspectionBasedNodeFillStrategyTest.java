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
import com.ejisto.modules.gui.components.tree.InspectionBasedNodeFillStrategy;
import com.ejisto.modules.gui.components.tree.NodeFillStrategy;
import com.ejisto.modules.gui.components.tree.node.ClassNode;
import com.ejisto.modules.gui.components.tree.node.FieldNode;
import com.ejisto.modules.gui.components.tree.node.RootNode;
import com.ejisto.util.GuiUtils;
import com.ejisto.util.IteratorEnumeration;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import javax.swing.tree.MutableTreeNode;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicInteger;

import static java.util.Arrays.asList;
import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

/**
 * Created by IntelliJ IDEA.
 * User: celestino
 * Date: 1/31/12
 * Time: 7:22 PM
 */
public class InspectionBasedNodeFillStrategyTest {
    @Mock private ClassNode root;
    @Mock private MockedField field1;
    @Mock private MockedField field2;
    @Mock private MockedField field3;
    @Mock private MockedField field4;


    @Before
    public void initMocks() {
        MockitoAnnotations.initMocks(this);
        when(root.getNodePath()).thenReturn(new String[]{"/ejisto-test"});
        when(root.isRoot()).thenReturn(true);
    }

    @Test
    public void testFillFirstChildAndSearch() throws Exception {
        String[] field1Path = new String[]{"/ejisto-test", "this", "is", "a", "test", "TestClass"};
        when(field1.getParentClassPath()).thenReturn(field1Path);
        when(field1.getPath()).thenReturn(
                new String[]{"/ejisto-test", "this", "is", "a", "test", "TestClass", "testProperty"});
        when(field1.getParentClassPathAsString()).thenReturn("/ejisto-test>this>is>a>test>TestClass");

        String[] field2Path = new String[]{"/ejisto-test"};
        when(field2.getParentClassPath()).thenReturn(field2Path);
        when(field2.getPath()).thenReturn(
                new String[]{"/ejisto-test", "this", "is", "a", "test", "TestClass", "testProperty2"});
        when(field2.getParentClassPathAsString()).thenReturn("/ejisto-test");

        String[] field3Path = new String[]{"/ejisto-test", "another", "test", "TestClass"};
        when(field3.getParentClassPath()).thenReturn(field3Path);
        when(field3.getPath()).thenReturn(
                new String[]{"/ejisto-test", "this", "was", "another", "test", "TestClass", "testProperty3"});
        when(field3.getParentClassPathAsString()).thenReturn("/ejisto-test>this>was>another>test>TestClass");


        when(root.getUserObject()).thenReturn(field2);
        final IteratorEnumeration<FieldNode> enumeration = new IteratorEnumeration<>(
                asList((FieldNode) new ClassNode(field3, new String[]{"/ejisto-test", "this"})).iterator());
        when(root.children()).thenReturn(enumeration);
        NodeFillStrategy strategy = new InspectionBasedNodeFillStrategy();
        FieldNode node = strategy.insertField(root, field1);
        verify(root, never()).add(any(FieldNode.class));
        assertNotNull(node);
        assertEquals(GuiUtils.encodeTreePath(field1.getPath()), GuiUtils.encodeTreePath(node.getNodePath()));
        assertTrue(strategy.containsChild(root, field1));
    }

    @Test
    public void testNodeNotFound() {
        NodeFillStrategy strategy = new InspectionBasedNodeFillStrategy();
        when(field1.getParentClassPath()).thenReturn(
                new String[]{"/ejisto-test", "this", "is", "a", "test", "TestClass"});
        when(field1.getPath()).thenReturn(
                new String[]{"/ejisto-test", "this", "is", "a", "test", "TestClass", "testProperty"});
        when(field1.getParentClassPathAsString()).thenReturn("/ejisto-test>this>is>a>test>TestClass");
        when(field2.getParentClassPath()).thenReturn(
                new String[]{"/ejisto-test2", "this", "is", "another", "test", "TestClass"});
        when(field2.getPath()).thenReturn(
                new String[]{"/ejisto-test", "this", "is", "another", "test", "TestClass", "testProperty2"});
        when(field2.getParentClassPathAsString()).thenReturn("/ejisto-test2>this>is>another>test>TestClass");
        when(root.getUserObject()).thenReturn(field2);
        assertTrue(strategy.containsChild(root, field1));
        ClassNode node2 = new ClassNode(field2, field2.getParentClassPath());
        assertFalse(strategy.containsChild(node2, field1));
    }

    @Test
    public void testFillRootNodeWithEqualFields() throws Exception {
        when(field1.getParentClassPath()).thenReturn(
                new String[]{"/ejisto-test", "this", "is", "a", "test", "TestClass"});
        when(field1.getParentClassPathAsString()).thenReturn("/ejisto-test>this>is>a>test>TestClass");
        when(field1.getPath()).thenReturn(
                new String[]{"/ejisto-test", "this", "is", "a", "test", "TestClass", "testProperty"});
        when(root.children()).thenReturn(
                new IteratorEnumeration<>(Collections.<FieldNode>emptyList().iterator()));
        final AtomicInteger counter = new AtomicInteger();
        RootNode root1 = new RootNode() {
            @Override
            public void add(MutableTreeNode newChild) {
                if (counter.incrementAndGet() == 2) {
                    fail();
                }
                super.add(newChild);
            }
        };
        NodeFillStrategy strategy = new InspectionBasedNodeFillStrategy();
        strategy.insertField(root1, field1);
        strategy.insertField(root1, field1);
        assertEquals(1, root1.getChildCount());
    }

    @Test
    public void testFillRootNodeWithDifferentFieldsButSameContext() {
        when(field1.getParentClassPath()).thenReturn(
                new String[]{"/ejisto-test", "this", "is", "a", "test", "TestClass"});
        when(field1.getPath()).thenReturn(
                new String[]{"/ejisto-test", "this", "is", "a", "test", "TestClass", "testProperty"});
        when(field1.getParentClassPathAsString()).thenReturn("/ejisto-test>this>is>a>test>TestClass");

        when(field2.getParentClassPath()).thenReturn(
                new String[]{"/ejisto-test", "this", "is", "a", "test", "TestClass2"});
        when(field2.getPath()).thenReturn(
                new String[]{"/ejisto-test", "this", "is", "a", "test", "TestClass2", "testProperty"});
        when(field2.getParentClassPathAsString()).thenReturn("/ejisto-test>this>is>a>test>TestClass2");
        NodeFillStrategy strategy = new InspectionBasedNodeFillStrategy();
        final AtomicInteger counter = new AtomicInteger();
        RootNode root1 = new RootNode() {
            @Override
            public void add(MutableTreeNode newChild) {
                if (counter.incrementAndGet() == 2) {
                    fail();
                }
                super.add(newChild);
            }
        };
        strategy.insertField(root1, field1);
        strategy.insertField(root1, field2);

    }

    @Test
    public void testFillRootNodeWithDifferentContexts() {
        when(field1.getParentClassPath()).thenReturn(
                new String[]{"/ejisto-test", "this", "is", "a", "test", "TestClass"});
        when(field1.getPath()).thenReturn(
                new String[]{"/ejisto-test", "this", "is", "a", "test", "TestClass", "testProperty"});
        when(field1.getParentClassPathAsString()).thenReturn("/ejisto-test>this>is>a>test>TestClass");

        when(field2.getParentClassPath()).thenReturn(
                new String[]{"/ejisto-test-1", "this", "is", "a", "test", "TestClass2"});
        when(field2.getPath()).thenReturn(
                new String[]{"/ejisto-test", "this", "is", "a", "test", "TestClass2", "testProperty"});
        when(field2.getParentClassPathAsString()).thenReturn("/ejisto-test-1>this>is>a>test>TestClass2");

        when(field3.getParentClassPath()).thenReturn(
                new String[]{"/ejisto-test-1", "this", "is", "a", "test", "TestClass3"});
        when(field3.getPath()).thenReturn(
                new String[]{"/ejisto-test", "this", "is", "a", "test", "TestClass3", "testProperty"});
        when(field3.getParentClassPathAsString()).thenReturn("/ejisto-test-1>this>is>a>test>TestClass3");
        NodeFillStrategy strategy = new InspectionBasedNodeFillStrategy();
        final AtomicInteger counter = new AtomicInteger();
        RootNode root1 = new RootNode() {
            @Override
            public void add(MutableTreeNode newChild) {
                if (counter.incrementAndGet() == 3) {
                    fail();
                }
                super.add(newChild);
            }
        };
        strategy.insertField(root1, field1);
        strategy.insertField(root1, field2);
        strategy.insertField(root1, field3);
    }
}
