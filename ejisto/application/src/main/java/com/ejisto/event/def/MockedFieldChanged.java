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

package com.ejisto.event.def;

import com.ejisto.modules.dao.entities.MockedField;

public class MockedFieldChanged extends BaseApplicationEvent {
    private static final long serialVersionUID = -1695827582666783071L;

    private MockedField mockedField;

    public MockedFieldChanged(Object source, MockedField mockedField) {
        super(source);
        this.mockedField = mockedField;
    }

    public MockedField getMockedField() {
        return mockedField;
    }

    @Override
    public String getDescription() {
        return mockedField + " changed";
    }

    @Override
    public String getKey() {
        return null;
    }
}
