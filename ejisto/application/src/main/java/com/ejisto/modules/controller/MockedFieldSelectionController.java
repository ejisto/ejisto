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

package com.ejisto.modules.controller;

import ch.lambdaj.function.closure.Closure0;
import com.ejisto.modules.dao.entities.MockedField;
import com.ejisto.modules.gui.components.Header;
import com.ejisto.modules.gui.components.MockedFieldsEditor;
import com.ejisto.modules.gui.components.helper.CallbackAction;
import org.jdesktop.swingx.JXDialog;
import org.jdesktop.swingx.JXPanel;

import javax.swing.*;
import java.awt.*;
import java.util.List;

import static com.ejisto.modules.gui.components.helper.FieldsEditorContext.ADD_FIELD;
import static com.ejisto.util.GuiUtils.centerOnScreen;
import static com.ejisto.util.GuiUtils.getMessage;
import static java.util.Collections.emptyList;

/**
 * Created by IntelliJ IDEA.
 * User: celestino
 * Date: 11/8/11
 * Time: 5:48 PM
 */
public class MockedFieldSelectionController {

    private MockedFieldsEditorController controller;
    private Closure0 closeAction;
    private Closure0 okAction;
    private JXDialog dialog;
    private List<MockedField> selectedFields;

    public MockedFieldSelectionController() {
        this.controller = new MockedFieldsEditorController(ADD_FIELD);
    }

    private void initClosures() {
        closeAction = new Closure0() {{of(MockedFieldSelectionController.this).close();}};
        okAction = new Closure0() {{of(MockedFieldSelectionController.this).ok();}};
    }

    void close() {
        this.selectedFields = emptyList();
        dialog.setVisible(false);
    }

    void ok() {
        this.selectedFields = controller.getSelection();
        dialog.setVisible(false);
    }

    public void showSelectionDialog() {
        initClosures();
        dialog = new JXDialog(createPanel());
        dialog.setPreferredSize(new Dimension(400, 300));
        dialog.setSize(new Dimension(400, 300));
        dialog.setResizable(true);
        dialog.setModal(true);
        centerOnScreen(dialog);
        dialog.setVisible(true);
    }

    private JPanel createPanel() {
        JXPanel content = new JXPanel(new BorderLayout(0, 0));
        content.add(new Header(getMessage("field.create.dialog.title"), getMessage("field.create.dialog.description")),
                    BorderLayout.NORTH);
        MockedFieldsEditor view = controller.getView();
        ActionMap actionMap = content.getActionMap();
        actionMap.put(JXDialog.CLOSE_ACTION_COMMAND, new CallbackAction("close", closeAction));
        actionMap.put(JXDialog.EXECUTE_ACTION_COMMAND, new CallbackAction("ok", okAction));
        content.add(view, BorderLayout.CENTER);
        return content;
    }

    public List<MockedField> getSelectedFields() {
        return selectedFields;
    }

}
