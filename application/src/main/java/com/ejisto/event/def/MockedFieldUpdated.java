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

package com.ejisto.event.def;

import com.ejisto.modules.dao.entities.MockedField;

import java.util.Arrays;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: celestino
 * Date: 1/15/14
 * Time: 6:56 PM
 */
public class MockedFieldUpdated extends MockedFieldChanged {

    private static final long serialVersionUID = 1L;

    public MockedFieldUpdated(Object source, MockedField mockedField) {
        this(source, Arrays.asList(mockedField));
    }

    public MockedFieldUpdated(Object source, List<MockedField> mockedField) {
        super(source, mockedField);
    }

}
