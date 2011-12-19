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
import com.ejisto.modules.gui.components.MockedFieldsEditorComponent;

/**
 * Created by IntelliJ IDEA.
 * User: celestino
 * Date: 11/2/11
 * Time: 6:32 PM
 */
public class MockedFieldOperation extends BaseApplicationEvent {

    private OperationType operationType;
    private MockedField mockedField;

    public enum OperationType {
        CREATE("propertieseditor.popup.add", "propertieseditor.popup.add.icon"),
        UPDATE("propertieseditor.popup.edit", "propertieseditor.popup.edit.icon"),
        DELETE("propertieseditor.popup.delete", "propertieseditor.popup.delete.icon");
        private final String key;
        private final String icon;

        private OperationType(String key, String icon) {
            this.key = key;
            this.icon = icon;
        }

        public String getKey() {
            return key;
        }

        public String getIcon() {
            return icon;
        }
    }

    public MockedFieldOperation(Object source, OperationType operationType, MockedField mockedField) {
        super(source);
        this.operationType = operationType;
        this.mockedField = mockedField;
    }

    @Override
    public String getDescription() {
        return operationType.getKey();
    }

    @Override
    public String getKey() {
        return operationType.getKey();
    }

    @Override
    public String getIconKey() {
        return operationType.getIcon();
    }

    @Override
    public MockedFieldsEditorComponent getSource() {
        return (MockedFieldsEditorComponent) super.getSource();
    }

    public OperationType getOperationType() {
        return operationType;
    }

    public MockedField getMockedField() {
        return mockedField;
    }
}
