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

import com.ejisto.event.def.MockedFieldChanged;
import com.ejisto.event.def.ServerRestartRequired;
import com.ejisto.modules.dao.entities.MockedField;
import com.ejisto.modules.dao.entities.MockedFieldImpl;
import com.ejisto.modules.gui.components.helper.FieldEditorPanel;
import com.ejisto.modules.gui.components.helper.FieldsEditorContext;
import com.ejisto.modules.repository.MockedFieldsRepository;
import org.jdesktop.swingx.action.AbstractActionExt;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.ejisto.constants.StringConstants.DEFAULT_CONTAINER_ID;
import static com.ejisto.util.GuiUtils.getMessage;
import static com.ejisto.util.GuiUtils.publishEvent;

/**
 * Created by IntelliJ IDEA.
 * User: celestino
 * Date: 6/1/12
 * Time: 8:08 AM
 */
public class MockedFieldCreationController extends AbstractDialogManager {

    public static final String CTX_SELECTION = "ctxSelection";
    private final JPanel view;
    private final FieldEditorPanel fieldEditorPanel;
    private final MockedFieldsRepository mockedFieldsRepository;

    public MockedFieldCreationController(MockedFieldsRepository mockedFieldsRepository) {
        super();
        this.mockedFieldsRepository = mockedFieldsRepository;
        view = new JPanel(new BorderLayout());
        ActionMap map = new ActionMap();
        map.put(CTX_SELECTION, new AbstractActionExt() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setTypes(fieldEditorPanel.getContextPath());
            }
        });
        fieldEditorPanel = new FieldEditorPanel(map, FieldsEditorContext.CREATE_FIELD);
        final Optional<String> first = setAllAvailableContextPaths();
        if(first.isPresent()) {
            setTypes(first.get());
        }
        view.add(fieldEditorPanel, BorderLayout.CENTER);
    }

    public void showCreateDialog() {
        int width = 500;
        int height = 400;
        openDialog(view, getMessage("create.new.field.title"), getMessage("create.new.field.description"),
                   "field.create.icon", new Dimension(width, height));
    }

    @Override
    void onAbort() {
    }

    @Override
    void onConfirm() {
        MockedField mf = new MockedFieldImpl();
        mf.setActive(true);
        mf.setFieldName(fieldEditorPanel.getFieldName());
        mf.setFieldValue(fieldEditorPanel.getFieldValue());
        mf.setClassName(fieldEditorPanel.getFieldClass());
        mf.setContextPath(fieldEditorPanel.getContextPath());
        mf.setFieldType(fieldEditorPanel.getFieldType());
        mockedFieldsRepository.insert(mf);
        MockedFieldChanged event = new MockedFieldChanged(this,
                                                          mockedFieldsRepository.load(mf.getContextPath(),
                                                                                      mf.getClassName(),
                                                                                      mf.getFieldName()));
        publishEvent(event);
        publishEvent(new ServerRestartRequired(DEFAULT_CONTAINER_ID.getValue(), this));
    }

    private void setTypes(String selectedContextPath) {
        Set<String> contexts = mockedFieldsRepository.loadAll(selectedContextPath, FieldsEditorContext.CREATE_FIELD::isAdmitted)
                .stream()
                .map(MockedField::getClassName)
                .collect(Collectors.toSet());
        fieldEditorPanel.setTypes(contexts);
    }

    private Optional<String> setAllAvailableContextPaths() {
        final Stream<String> stream = mockedFieldsRepository.loadAll()
                .stream()
                .map(MockedField::getContextPath);
        final Optional<String> first = stream.findFirst();
        fieldEditorPanel.setAvailableContextPaths(stream.collect(Collectors.toSet()));
        return first;
    }
}
