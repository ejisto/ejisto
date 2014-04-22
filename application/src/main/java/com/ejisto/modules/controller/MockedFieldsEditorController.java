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

package com.ejisto.modules.controller;

import com.ejisto.event.ApplicationListener;
import com.ejisto.event.def.ApplicationDeployed;
import com.ejisto.event.def.WebAppContextStatusChanged;
import com.ejisto.modules.dao.entities.MockedField;
import com.ejisto.modules.gui.components.MockedFieldsEditor;
import com.ejisto.modules.gui.components.helper.*;
import com.ejisto.modules.repository.MockedFieldsRepository;
import com.ejisto.util.WebAppContextStatusCommand;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.ejisto.modules.gui.components.helper.EditorType.FLATTEN;
import static com.ejisto.modules.gui.components.helper.EditorType.HIERARCHICAL;
import static com.ejisto.modules.gui.components.helper.FieldsEditorContext.APPLICATION_INSTALLER_WIZARD;
import static com.ejisto.util.GuiUtils.*;

/**
 * Created by IntelliJ IDEA.
 * User: celestino
 * Date: 12/28/10
 * Time: 5:22 PM
 */
public class MockedFieldsEditorController implements ActionListener, FieldEditingListener {
    public static final String STOP_EDITING = "STOP_EDITING";
    public static final String CANCEL_EDITING = "CANCEL_EDITING";
    private final Predicate<MockedField> filterByEditorContext;
    private final MockedFieldsRepository mockedFieldsRepository;
    private final ReentrantLock lock;
    private MockedField editedField;
    private Point currentEditingLocation;
    private MockedFieldsEditor view;
    private ActionMap actionMap;
    private Collection<MockedField> wizardFields = Collections.emptyList();
    private volatile int selectedIndex = 0;

    public MockedFieldsEditorController(MockedFieldsRepository mockedFieldsRepository) {
        this(mockedFieldsRepository, APPLICATION_INSTALLER_WIZARD);
    }

    public MockedFieldsEditorController(MockedFieldsRepository mockedFieldsRepository, FieldsEditorContext fieldsEditorContext) {
        this.mockedFieldsRepository = mockedFieldsRepository;
        this.filterByEditorContext = fieldsEditorContext::isAdmitted;
        actionMap = new ActionMap();
        initActions();
        view = new MockedFieldsEditor(fieldsEditorContext, getActionMap());
        view.registerChangeListener(this);
        view.registerMouseLister(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (e.getClickCount() != 2) {
                    return;
                }
                setCurrentEditingLocation(e.getPoint());
                startEdit(null, e.getPoint());
            }
        });
        if (fieldsEditorContext != APPLICATION_INSTALLER_WIZARD) {
            view.setFields(mockedFieldsRepository.loadAll(fieldsEditorContext::isAdmitted));
        }
        view.registerFieldEditingListener(this);
        lock = new ReentrantLock();
        registerApplicationEventListener(new ApplicationListener<ApplicationDeployed>() {
            @Override
            public void onApplicationEvent(final ApplicationDeployed event) {
                runOnEDT(() -> notifyContextInstalled(event.getContext()));
            }

            @Override
            public Class<ApplicationDeployed> getTargetEventType() {
                return ApplicationDeployed.class;
            }
        });

        registerApplicationEventListener(new ApplicationListener<WebAppContextStatusChanged>() {
            @Override
            public void onApplicationEvent(final WebAppContextStatusChanged event) {
                runOnEDT(() -> {
                    if (event.getCommand() == WebAppContextStatusCommand.DELETE) {
                        notifyContextDeleted(
                                event.getContextPath());
                    }
                });
            }

            @Override
            public Class<WebAppContextStatusChanged> getTargetEventType() {
                return WebAppContextStatusChanged.class;
            }
        });
    }

    private void notifyContextInstalled(String contextPath) {
        view.contextInstalled(contextPath, mockedFieldsRepository.loadActiveFields(contextPath,filterByEditorContext));
    }

    private void notifyContextDeleted(String contextPath) {
        view.contextRemoved(contextPath, mockedFieldsRepository.loadAll(contextPath,filterByEditorContext));
    }

    private void startEdit(MockedFieldEditingEvent event, Point editingPoint) {
        editedField = event != null ? event.getField() : getView().getMockedFieldAt(editingPoint.x, editingPoint.y,
                                                                                    selectedIndex == 0);
        if (editedField != null && !editedField.isSimpleValue()) {
            editingStarted();
        } else {
            editingCanceled();
        }
    }

    public MockedFieldsEditor getView() {
        return view;
    }

    void selectionChanged(int selectedIndex) {
        this.selectedIndex = selectedIndex;
        view.showCard(EditorType.fromIndex(selectedIndex).orElseThrow(IllegalArgumentException::new));
    }

    final ActionMap getActionMap() {
        return actionMap;
    }

    public List<MockedField> getSelection() {
        if (selectedIndex == 0) {
            return getView().getTableSelectedItems();
        }
        return getView().getTreeSelectedItems();
    }

    private void initActions() {
        final ActionHelper actionHelper = new ActionHelper();

        actionMap.put(STOP_EDITING, new CallbackAction(getMessage("ok"), (e) -> actionHelper.editingStopped()));
        actionMap.put(CANCEL_EDITING, new CallbackAction(getMessage("cancel"), (e) -> actionHelper.editingCanceled()));
        actionMap.put(HIERARCHICAL.toString(), new CallbackAction(HIERARCHICAL.toString(), (e) -> actionHelper.hierarchicalSelected()));
        actionMap.put(FLATTEN.toString(), new CallbackAction(FLATTEN.toString(), (e) -> actionHelper.flattenSelected()));
    }

    void editingStarted() {
        if (lock.isLocked()) {
            return;
        }
        lock.tryLock();
        getView().initEditorPanel(selectMockedFieldTypes(),
                                  getMessage("wizard.properties.editor.complex.title", editedField.getFieldName(),
                                             editedField.getClassSimpleName()),
                                  editedField);
        getView().expandCollapseEditorPanel(true);
        getView().setFocusOnComplexEditorPanel();
    }

    void editingStopped() {
        if (!lock.isLocked()) {
            //probably called while panel is collapsing
            return;
        }
        getView().expandCollapseEditorPanel(false);
        editedField.setFieldElementType(getView().getFieldType());
        editedField.setExpression(buildExpression());
        Point p = getCurrentEditingLocation();
        getView().getTree().redraw(p.x, p.y);
        getView().requestFocusOnActiveEditor(EditorType.fromIndex(selectedIndex).orElseThrow(IllegalArgumentException::new));
        lock.unlock();
    }

    private String buildExpression() {
        return null;
    }

    void editingCanceled() {
        getView().expandCollapseEditorPanel(false);
        if (lock.isLocked()) {
            lock.unlock();
        }
    }

    private Collection<String> selectMockedFieldTypes() {
        Collection<MockedField> fields = new ArrayList<>(wizardFields);
        fields.addAll(mockedFieldsRepository.loadAll(filterByEditorContext));
        return fields.stream().map(MockedField::getClassName).collect(Collectors.toSet());
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e == null) {
            return;
        }
        Action action = getActionMap().get(e.getActionCommand());
        if (action != null) {
            action.actionPerformed(e);
        }
    }

    public void setWizardFields(Collection<MockedField> wizardFields) {
        this.wizardFields = wizardFields;
    }

    @Override
    public void editingStarted(MockedFieldEditingEvent event) {
        setCurrentEditingLocation(event.getPoint());
        startEdit(event, getCurrentEditingLocation());
    }

    @Override
    public void editingStopped(MockedFieldEditingEvent event) {
    }

    @Override
    public void editingCanceled(MockedFieldEditingEvent event) {
    }

    Point getCurrentEditingLocation() {
        return currentEditingLocation;
    }

    void setCurrentEditingLocation(Point currentEditingLocation) {
        this.currentEditingLocation = currentEditingLocation;
    }

    private class ActionHelper {

        void editingStopped() {
            MockedFieldsEditorController.this.editingStopped();
        }

        void editingCanceled() {
            MockedFieldsEditorController.this.editingCanceled();
        }

        void hierarchicalSelected() {
            MockedFieldsEditorController.this.selectionChanged(HIERARCHICAL.getIndex());
        }

        void flattenSelected() {
            MockedFieldsEditorController.this.selectionChanged(FLATTEN.getIndex());
        }

    }
}
