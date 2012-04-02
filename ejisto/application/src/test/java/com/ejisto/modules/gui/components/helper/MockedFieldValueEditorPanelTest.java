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

import org.junit.Test;

import static com.ejisto.modules.gui.components.helper.MockedFieldValueEditorPanel.abbreviate;
import static org.junit.Assert.assertEquals;

/**
 * Created by IntelliJ IDEA.
 * User: celestino
 * Date: 4/2/12
 * Time: 8:05 AM
 */
public class MockedFieldValueEditorPanelTest {
    @Test
    public void testAbbreviate() {
        assertEquals("[..].package2.Test", abbreviate("com.test.package1.package2.Test"));
        assertEquals("com.package2.Test", abbreviate("com.package2.Test"));
        assertEquals("[..].package2.Test", abbreviate("com.package1.package2.Test"));
    }
}
