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

package com.ejisto.core.classloading.util;

import org.junit.Test;

import static com.ejisto.core.classloading.util.ReflectionUtils.cleanGenericSignature;
import static com.ejisto.core.classloading.util.ReflectionUtils.getFieldName;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNull;

public class ReflectionUtilsTest {
    @Test
    public void testMethodExtraction() {
        assertEquals("pippoBaudo", getFieldName("getPippoBaudo"));
        assertEquals("pippoBaudo", getFieldName("isPippoBaudo"));
        assertEquals("pippoBaudo", getFieldName("setPippoBaudo"));
        assertNull(getFieldName("whoIsPippoBaudo"));
    }

    @Test
    public void testGenericSignatureCleaning() {
        assertEquals("com.test.EjistoTest", cleanGenericSignature("? extends com.test.EjistoTest"));
        assertEquals("com.test.EjistoTest",
                     cleanGenericSignature("com.test.EjistoTest <? extends com.test.EjistoTest>"));
        assertEquals("com.test.EjistoTest, com.test.EjistoTest<com.test.EjistoTest>",
                     cleanGenericSignature("? extends com.test.EjistoTest, com.test.EjistoTest<com.test.EjistoTest>"));
    }
}
