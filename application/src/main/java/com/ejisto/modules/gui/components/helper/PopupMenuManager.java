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

package com.ejisto.modules.gui.components.helper;

import com.ejisto.event.def.MockedFieldOperation;
import com.ejisto.modules.dao.entities.MockedField;
import com.ejisto.modules.gui.EjistoAction;
import com.ejisto.modules.gui.components.MockedFieldsEditorComponent;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import static com.ejisto.event.def.MockedFieldOperation.OperationType.*;
import static com.ejisto.util.GuiUtils.getIcon;
import static com.ejisto.util.GuiUtils.getMessage;

public class PopupMenuManager extends MouseAdapter {

    public PopupMenuManager() {
    }

    @Override
    public void mousePressed(MouseEvent e) {
        handleClick(e);
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        handleClick(e);
    }

    private void handleClick(final MouseEvent e) {
        if (e.isPopupTrigger()) {
            final MockedFieldsEditorComponent editor = (MockedFieldsEditorComponent) e.getComponent();
            editor.selectFieldAt(e.getPoint());
            JPopupMenu menu = new JPopupMenu();
            menu.setInvoker(editor.toComponent());
            if(editor.fillWithCustomMenuItems(menu, e.getPoint())) {
                menu.addSeparator();
            }
            fillWithAddActions(menu, editor);
            if (isLocationEditable(editor, e.getPoint())) {
                MockedField target = getFieldAt(editor, e.getPoint());
                //update button
                menu.add(new AbstractAction(getMessage(UPDATE.getKey()), getIcon(UPDATE.getIcon())) {
                    @Override
                    public void actionPerformed(ActionEvent ev) {
                        Point point = e.getPoint();
                        editor.editFieldAt(point);
                    }
                });
                menu.add(buildEjistoAction(editor, DELETE, target));
            }
            menu.show(e.getComponent(), e.getX(), e.getY());
        }
    }

    private void fillWithAddActions(JPopupMenu menu, MockedFieldsEditorComponent editor) {
        if (editor.getCurrentEditorContext() == FieldsEditorContext.MAIN_WINDOW) {
            menu.add(buildEjistoAction(editor, ADD, null));
            menu.add(buildEjistoAction(editor, CREATE, null));
        }
    }

    private MockedField getFieldAt(MockedFieldsEditorComponent component, Point p) {
        return component.getFieldAt(p);
    }

    private boolean isLocationEditable(MockedFieldsEditorComponent component, Point p) {
        return component.hasEditableFieldAtLocation(p);
    }

    private EjistoAction<MockedFieldOperation> buildEjistoAction(MockedFieldsEditorComponent component, MockedFieldOperation.OperationType operationType, MockedField field) {
        return new EjistoAction<>(new MockedFieldOperation(component, operationType, field));
    }

}