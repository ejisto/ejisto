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

package com.ejisto.modules.gui.components;

import com.ejisto.modules.dao.entities.MockedField;
import com.ejisto.modules.gui.components.helper.FieldEditingListener;

import java.awt.*;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: celestino
 * Date: 10/30/11
 * Time: 3:35 PM
 */
public interface MockedFieldsEditorComponent {

    MockedField getFieldAt(Point point);

    MockedField getFieldAt(int x, int y);

    void setFields(List<MockedField> fields);

    void editFieldAt(Point point);

    void selectFieldAt(Point point);

    List<MockedField> getSelectedFields();

    void addFieldEditingListener(FieldEditingListener fieldEditingListener);

    void removeFieldEditingListener(FieldEditingListener fieldEditingListener);

}
