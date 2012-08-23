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

package com.ejisto.event.def;

import com.ejisto.modules.dao.entities.MockedField;

import java.util.Arrays;
import java.util.List;

public class MockedFieldChanged extends BaseApplicationEvent {
    private static final long serialVersionUID = -1695827582666783071L;

    private final List<MockedField> mockedFields;

    public MockedFieldChanged(Object source, MockedField mockedField) {
        this(source, Arrays.asList(mockedField));
    }

    public MockedFieldChanged(Object source, List<MockedField> mockedField) {
        super(source);
        this.mockedFields = mockedField;
    }

    public List<MockedField> getMockedFields() {
        return mockedFields;
    }

    @Override
    public String getDescription() {
        return "mockedFields: " + mockedFields + " changed";
    }

    @Override
    public String getKey() {
        return null;
    }
}
