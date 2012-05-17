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

import com.ejisto.modules.dao.entities.MockedField;
import com.ejisto.util.GuiUtils;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import static java.util.Arrays.asList;
import static java.util.EnumSet.allOf;
import static java.util.EnumSet.of;

/**
 * Created by IntelliJ IDEA.
 * User: celestino
 * Date: 11/7/11
 * Time: 8:52 AM
 */
public enum FieldsEditorContext {
    MAIN_WINDOW("fields.table.model.MAIN_WINDOW.columns", new GuiUtils.EditorColumnFillStrategy() {
        @Override
        public void fillRow(List<List<String>> rows, MockedField row) {
            rows.add(asList(String.valueOf(row.getId()),
                            row.getContextPath(),
                            row.getClassName(),
                            row.getFieldName(),
                            row.getFieldType(),
                            row.getFieldValue()));
        }
    }, true, true, allOf(EditorType.class)) {
        @Override
        public boolean isAdmitted(MockedField mockedField) {
            return mockedField.isActive();
        }
    },

    APPLICATION_INSTALLER_WIZARD("fields.table.model.APPLICATION_INSTALLER_WIZARD.columns",
                                 new GuiUtils.EditorColumnFillStrategy() {
                                     @Override
                                     public void fillRow(List<List<String>> rows, MockedField row) {
                                         rows.add(asList(row.getClassName(),
                                                         row.getFieldName(),
                                                         row.getFieldType(),
                                                         row.getFieldValue()));
                                     }
                                 }, false, true, allOf(EditorType.class)) {
        @Override
        public boolean isAdmitted(MockedField mockedField) {
            return true;
        }
    },

    ADD_FIELD("fields.table.model.ADD_FIELD.columns", new GuiUtils.EditorColumnFillStrategy() {
        @Override
        public void fillRow(List<List<String>> rows, MockedField row) {
            rows.add(asList(row.getClassName(),
                            row.getFieldName(),
                            row.getFieldType(),
                            row.getFieldValue()));
        }
    }, false, false, of(EditorType.FLATTEN)) {
        @Override
        public boolean isAdmitted(MockedField mockedField) {
            return !mockedField.isActive();
        }
    };

    private String tableColumnsKey;
    private transient GuiUtils.EditorColumnFillStrategy editorColumnFillStrategy;
    private boolean notifyChangeNeeded;
    private boolean editable;
    private Set<EditorType> supportedEditors;

    private FieldsEditorContext(String tableColumnsKey, GuiUtils.EditorColumnFillStrategy editorColumnFillStrategy,
                                boolean notifyChangeNeeded, boolean editable, Set<EditorType> supportedEditors) {
        this.tableColumnsKey = tableColumnsKey;
        this.editorColumnFillStrategy = editorColumnFillStrategy;
        this.notifyChangeNeeded = notifyChangeNeeded;
        this.editable = editable;
        this.supportedEditors = supportedEditors;
    }

    public abstract boolean isAdmitted(MockedField mockedField);

    public String[] getTableColumns() {
        return GuiUtils.getMessage(tableColumnsKey).split(",");
    }

    public GuiUtils.EditorColumnFillStrategy getColumnFillStrategy() {
        return editorColumnFillStrategy;
    }

    public boolean isNotifyChangeNeeded() {
        return notifyChangeNeeded;
    }

    public boolean isEditable() {
        return editable;
    }

    public Collection<EditorType> getSupportedEditors() {
        return supportedEditors;
    }

}
