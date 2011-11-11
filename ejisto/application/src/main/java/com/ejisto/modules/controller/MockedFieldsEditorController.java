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
import ch.lambdaj.function.convert.PropertyExtractor;
import com.ejisto.event.def.MockedFieldChanged;
import com.ejisto.modules.dao.entities.MockedField;
import com.ejisto.modules.gui.components.MockedFieldsEditor;
import com.ejisto.modules.gui.components.helper.CallbackAction;
import com.ejisto.modules.gui.components.helper.EditorType;
import com.ejisto.modules.gui.components.helper.FieldsEditorContext;
import com.ejisto.modules.repository.MockedFieldsRepository;
import com.ejisto.util.FieldsEditorContextMatcher;
import com.ejisto.util.GuiUtils;
import org.springframework.context.ApplicationListener;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.*;
import java.util.concurrent.locks.ReentrantLock;

import static ch.lambdaj.Lambda.convert;
import static com.ejisto.modules.gui.components.helper.EditorType.FLATTEN;
import static com.ejisto.modules.gui.components.helper.EditorType.HIERARCHICAL;
import static com.ejisto.modules.gui.components.helper.FieldsEditorContext.APPLICATION_INSTALLER_WIZARD;
import static com.ejisto.util.GuiUtils.getMessage;
import static com.ejisto.util.GuiUtils.runOnEDT;

/**
 * Created by IntelliJ IDEA.
 * User: celestino
 * Date: 12/28/10
 * Time: 5:22 PM
 */
public class MockedFieldsEditorController implements ActionListener {
    public static final String START_EDITING = "START_EDITING";
    public static final String STOP_EDITING = "STOP_EDITING";
    public static final String CANCEL_EDITING = "CANCEL_EDITING";
    private MockedField editedField;
    private ReentrantLock lock;
    private int x;
    private int y;
    private MockedFieldsEditor view;
    private ActionMap actionMap;
    private Collection<MockedField> wizardFields = Collections.emptyList();
    private volatile int selectedIndex = 0;
    private FieldsEditorContextMatcher contextMatcher;

    public MockedFieldsEditorController() {
        this(APPLICATION_INSTALLER_WIZARD);
    }

    public MockedFieldsEditorController(FieldsEditorContext fieldsEditorContext) {
        actionMap = new ActionMap();
        initActions();
        view = new MockedFieldsEditor(fieldsEditorContext);
        view.registerChangeListener(this);
        view.registerMouseLister(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (e.getClickCount() != 2) return;
                x = e.getX();
                y = e.getY();
                editedField = getView().getMockedFieldAt(x, y, selectedIndex == 0);
                if (editedField != null && !editedField.isSimpleValue()) {
                    editingStarted();
                } else {
                    editingCanceled();
                }
            }
        });
        view.initActionMap(getActionMap());
        contextMatcher = new FieldsEditorContextMatcher(fieldsEditorContext);
        view.setFields(MockedFieldsRepository.getInstance().loadAll(contextMatcher));
        lock = new ReentrantLock();
        GuiUtils.registerEventListener(MockedFieldChanged.class, new ApplicationListener<MockedFieldChanged>() {
            @Override
            public void onApplicationEvent(MockedFieldChanged event) {
                runOnEDT(new Runnable() {
                    @Override
                    public void run() {
                        view.setFields(MockedFieldsRepository.getInstance().loadAll(contextMatcher));
                    }
                });
            }
        });
    }

    public MockedFieldsEditor getView() {
        return view;
    }

    void selectionChanged(int selectedIndex) {
        this.selectedIndex = selectedIndex;
        if (selectedIndex == 0) {
            view.refreshTreeModel();
        } else {
            view.refreshFlattenTableModel();
        }
        view.showCard(EditorType.fromIndex(selectedIndex));
    }

    public ActionMap getActionMap() {
        return actionMap;
    }

    public List<MockedField> getSelection() {
        if (selectedIndex == 0) return getView().getTableSelectedItems();
        return getView().getTreeSelectedItems();
    }

    private void initActions() {
        actionMap.put(STOP_EDITING, new CallbackAction(STOP_EDITING, new Closure0() {{
            of(MockedFieldsEditorController.this).editingStopped();
        }}));
        actionMap.put(CANCEL_EDITING, new CallbackAction(CANCEL_EDITING, new Closure0() {{
            of(MockedFieldsEditorController.this).editingCanceled();
        }}));
        actionMap.put(HIERARCHICAL.toString(), new CallbackAction(HIERARCHICAL.toString(), new Closure0() {{
            of(MockedFieldsEditorController.this).selectionChanged(HIERARCHICAL.getIndex());
        }}));
        actionMap.put(FLATTEN.toString(), new CallbackAction(FLATTEN.toString(), new Closure0() {{
            of(MockedFieldsEditorController.this).selectionChanged(FLATTEN.getIndex());
        }}));
    }

    void editingStarted() {
        if (lock.isLocked()) return;
        lock.tryLock();
        getView().initEditorPanel(selectMockedFieldTypes(),
                                  getMessage("wizard.properties.editor.complex.title", editedField.getFieldName(),
                                             editedField.getClassSimpleName()),
                                  editedField);
        getView().expandCollapseEditorPanel(true);
    }

    void editingStopped() {
        getView().expandCollapseEditorPanel(false);
        editedField.setFieldElementType(getView().getFieldType());
        editedField.setExpression(buildExpression());
        getView().getTree().redraw(x, y);
        lock.unlock();
    }

    private String buildExpression() {
        return "size=" + getView().getFieldSize();
    }

    void editingCanceled() {
        getView().expandCollapseEditorPanel(false);
        if (lock.isLocked()) lock.unlock();
    }

    private Collection<String> selectMockedFieldTypes() {
        Collection<MockedField> fields = new ArrayList<MockedField>(wizardFields);
        fields.addAll(MockedFieldsRepository.getInstance().loadAll(contextMatcher));
        return new HashSet<String>(convert(fields, new PropertyExtractor<Object, String>("className")));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e == null) return;
        Action action = getActionMap().get(e.getActionCommand());
        if (action != null) action.actionPerformed(e);
    }

    public void setWizardFields(Collection<MockedField> wizardFields) {
        this.wizardFields = wizardFields;
    }

}
