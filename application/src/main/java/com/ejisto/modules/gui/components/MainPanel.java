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

package com.ejisto.modules.gui.components;

import com.ejisto.event.ApplicationEventDispatcher;
import com.ejisto.modules.controller.MockedFieldsEditorController;
import com.ejisto.modules.repository.MockedFieldsRepository;
import org.jdesktop.swingx.JXPanel;

import javax.swing.*;
import java.awt.*;

import static com.ejisto.modules.gui.components.helper.FieldsEditorContext.MAIN_WINDOW;

public class MainPanel extends JXPanel {
    private static final long serialVersionUID = -28148619997853619L;
    private final MockedFieldsRepository mockedFieldsRepository;
    private final ApplicationEventDispatcher eventDispatcher;
    private MockedFieldsEditorController propertiesEditor;

    public MainPanel(MockedFieldsRepository mockedFieldsRepository, ApplicationEventDispatcher eventDispatcher) {
        super();
        this.mockedFieldsRepository = mockedFieldsRepository;
        this.eventDispatcher = eventDispatcher;
        init();
    }

    private void init() {
        initLayout();
        initComponents();
    }

    private void initLayout() {
        setLayout(new BorderLayout());
    }

    private void initComponents() {
        setBackground(SystemColor.control);
        add(getPropertiesEditor(), BorderLayout.CENTER);
    }

    private MockedFieldsEditor getPropertiesEditor() {
        if (propertiesEditor != null) {
            return propertiesEditor.getView();
        }
        propertiesEditor = new MockedFieldsEditorController(mockedFieldsRepository, eventDispatcher, MAIN_WINDOW);
        MockedFieldsEditor view = propertiesEditor.getView();
        view.setBorder(BorderFactory.createTitledBorder(view.getName()));
        return view;
    }
}
