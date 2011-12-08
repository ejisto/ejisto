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

package com.ejisto.modules.gui.components.helper;

import com.ejisto.modules.dao.entities.MockedField;

import java.awt.*;
import java.util.EventObject;

/**
 * Created by IntelliJ IDEA.
 * User: celestino
 * Date: 11/17/11
 * Time: 8:52 AM
 */
public class MockedFieldEditingEvent extends EventObject {

    private MockedField field;
    private FieldsEditorContext editorContext;
    private Point point;

    public MockedFieldEditingEvent(Object source, MockedField field, FieldsEditorContext editorContext, Point point) {
        super(source);
        this.field = field;
        this.editorContext = editorContext;
        this.point = point;
    }

    public MockedField getField() {
        return field;
    }

    public FieldsEditorContext getEditorContext() {
        return editorContext;
    }

    public Point getPoint() {
        return point;
    }
}
