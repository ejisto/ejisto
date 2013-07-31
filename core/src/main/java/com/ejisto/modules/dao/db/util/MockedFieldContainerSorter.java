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

package com.ejisto.modules.dao.db.util;

import java.io.Serializable;
import java.util.Comparator;

/**
 * Created by IntelliJ IDEA.
 * User: celestino
 * Date: 3/6/13
 * Time: 8:19 AM
 */
public class MockedFieldContainerSorter implements Comparator<MockedFieldContainer>, Serializable {
    @Override
    public int compare(MockedFieldContainer o1, MockedFieldContainer o2) {
        int result = o1.getClassName().compareTo(o2.getClassName());
        if (result != 0) {
            return result;
        }
        return o1.getFieldName().compareTo(o2.getFieldName());
    }
}
