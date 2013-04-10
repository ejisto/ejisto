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

package com.ejisto.event.listener;

import com.ejisto.event.ApplicationListener;
import com.ejisto.event.def.MockedFieldChanged;
import com.ejisto.modules.dao.entities.MockedField;
import com.ejisto.modules.repository.MockedFieldsRepository;

public class FieldsUpdateListener implements ApplicationListener<MockedFieldChanged> {

    private final MockedFieldsRepository mockedFieldsRepository;

    public FieldsUpdateListener(MockedFieldsRepository mockedFieldsRepository) {
        this.mockedFieldsRepository = mockedFieldsRepository;
    }

    @Override
    public void onApplicationEvent(MockedFieldChanged event) {
        for (MockedField field : event.getMockedFields()) {
            handleFieldChange(field);
        }
    }

    @Override
    public Class<MockedFieldChanged> getTargetEvent() {
        return MockedFieldChanged.class;
    }

    private void handleFieldChange(MockedField field) {
        mockedFieldsRepository.insert(field);
    }

}
