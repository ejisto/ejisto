/*
 * Ejisto, a powerful developer assistant
 *
 * Copyright (C) 2010  Celestino Bellone
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
import ch.lambdaj.function.convert.PropertyExtractor;
import com.ejisto.modules.dao.entities.MockedField;
import com.ejisto.modules.gui.components.MockedFieldsEditor;
import com.ejisto.modules.gui.components.helper.CallbackAction;
import com.ejisto.modules.repository.MockedFieldsRepository;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Collection;
import java.util.HashSet;

import static ch.lambdaj.Lambda.convert;

/**
 * Created by IntelliJ IDEA.
 * User: celestino
 * Date: 12/28/10
 * Time: 5:22 PM
 */
public class MockedFieldsEditorController extends MouseAdapter implements ChangeListener {
    public static final String START_EDITING = "START_EDITING";
    public static final String STOP_EDITING = "STOP_EDITING";
    public static final String CANCEL_EDITING = "CANCEL_EDITING";

    private MockedFieldsEditor view;
    private ActionMap actionMap;

    public MockedFieldsEditorController() {
        this(false);
    }

    public MockedFieldsEditorController(boolean main) {
        actionMap = new ActionMap();
        initActions();
        view = new MockedFieldsEditor(main);
        view.registerChangeListener(this);
        view.initActionMap(getActionMap());
        view.setFields(MockedFieldsRepository.getInstance().loadAll());
    }

    public MockedFieldsEditor getView() {
        return view;
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        int selectedIndex = ((JTabbedPane) e.getSource()).getSelectedIndex();
        if (selectedIndex == 0) {
            view.refreshTreeModel();
        } else {
            view.refreshFlattenTableModel();
        }
    }

    public ActionMap getActionMap() {
        return actionMap;
    }

    @Override
    public void mousePressed(MouseEvent e) {
        if (e.getClickCount() != 2) return;
        MockedField mf = getView().getTree().getMockedFieldAt(e.getX(), e.getY());
        if (mf != null && !mf.isSimpleValue()) {
            editingStarted();
        } else {
            editingCanceled();
        }
    }

    private void initActions() {
        actionMap.put(START_EDITING, new CallbackAction(START_EDITING, new Closure0() {{
            of(MockedFieldsEditorController.this).editingStarted();
        }}));
        actionMap.put(STOP_EDITING, new CallbackAction(STOP_EDITING, new Closure0() {{
            of(MockedFieldsEditorController.this).editingStopped();
        }}));
        actionMap.put(CANCEL_EDITING, new CallbackAction(CANCEL_EDITING, new Closure0() {{
            of(MockedFieldsEditorController.this).editingCanceled();
        }}));
    }

    void editingStarted() {
        getView().initEditorPanel(selectMockedFieldTypes());
        getView().expandCollapseEditorPanel(true);
    }

    void editingStopped() {
        getView().expandCollapseEditorPanel(false);
    }

    void editingCanceled() {
        getView().expandCollapseEditorPanel(false);
    }

    private Collection<String> selectMockedFieldTypes() {
        return new HashSet<String>(convert(MockedFieldsRepository.getInstance().loadAll(), new PropertyExtractor<Object, String>("className")));
    }
}
