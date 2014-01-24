/*
 * Ejisto, a powerful developer assistant
 *
 * Copyright (C) 2010-2014 Celestino Bellone
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

package com.ejisto.modules.gui.components;

import org.jdesktop.swingx.JXList;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static java.util.Arrays.asList;
import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by IntelliJ IDEA.
 * User: celestino
 * Date: 1/24/14
 * Time: 7:58 AM
 */
public class ResourcesFilterTest {

    private static final List<String> RESOURCES = asList("1_First", "2_Second", "3_Third", "4_Fourth");
    private ResourcesFilter resourcesFilter;
    private JXList list;


    @Before
    public void init() {
        resourcesFilter = new ResourcesFilter();
        list = mock(JXList.class);
        resourcesFilter.setResourcesList(list);
        resourcesFilter.setResources(RESOURCES);
    }

    @Test
    public void testOne() throws Exception {
        when(list.getSelectedIndices()).thenReturn(new int[]{0, 1, 2});
        List<String> blacklistedObjects = resourcesFilter.getBlacklistedObjects();
        assertNotNull(blacklistedObjects);
        assertFalse(blacklistedObjects.isEmpty());
        assertEquals(asList("4_Fourth"), blacklistedObjects);
    }

    @Test
    public void testMultiple() throws Exception {
        when(list.getSelectedIndices()).thenReturn(new int[]{0, 2});
        List<String> blacklistedObjects = resourcesFilter.getBlacklistedObjects();
        assertNotNull(blacklistedObjects);
        assertFalse(blacklistedObjects.isEmpty());
        assertEquals(asList("2_Second", "4_Fourth"), blacklistedObjects);
    }

    @Test
    public void testAll() throws Exception {
        when(list.getSelectedIndices()).thenReturn(new int[]{0, 1, 2, 3});
        List<String> blacklistedObjects = resourcesFilter.getBlacklistedObjects();
        assertNotNull(blacklistedObjects);
        assertTrue(blacklistedObjects.isEmpty());
    }

    @Test
    public void testNone() throws Exception {
        when(list.getSelectedIndices()).thenReturn(new int[0]);
        List<String> blacklistedObjects = resourcesFilter.getBlacklistedObjects();
        assertNotNull(blacklistedObjects);
        assertFalse(blacklistedObjects.isEmpty());
        assertEquals(RESOURCES, blacklistedObjects);
    }
}
