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

package com.ejisto.modules.gui.components;

import com.ejisto.modules.repository.MockedFieldsRepository;
import org.jdesktop.swingx.JXPanel;
import org.jdesktop.swingx.JXTitledPanel;

import javax.swing.*;
import java.awt.*;

import static com.ejisto.util.GuiUtils.getMessage;

public class MainPanel extends JXPanel {
    private static final long serialVersionUID = -28148619997853619L;
    private MockedFieldsEditor propertiesEditor;
    private Header header;
    private JXTitledPanel editorContainer;
    private JXPanel widgetsPane;
    private JettyControl jettyControl;

    public MainPanel() {
        super();
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
        add(getHeader(), BorderLayout.NORTH);
        add(getWidgetsPane(), BorderLayout.CENTER);
    }

    private Header getHeader() {
        if (header != null) return header;
        header = new Header(getMessage("main.header.title"), getMessage("main.header.description"));
        return header;
    }

    private JXPanel getWidgetsPane() {
        if (this.widgetsPane != null) return this.widgetsPane;
        widgetsPane = new JXPanel(new BorderLayout());
        widgetsPane.add(getEditorContainer(), BorderLayout.CENTER);
        widgetsPane.add(getJettyControl(), BorderLayout.SOUTH);
        return widgetsPane;
    }

    private MockedFieldsEditor getPropertiesEditor() {
        if (propertiesEditor != null) return propertiesEditor;
        propertiesEditor = new MockedFieldsEditor(true);
        propertiesEditor.setFields(MockedFieldsRepository.getInstance().loadAll());
        return propertiesEditor;
    }

    private JXTitledPanel getEditorContainer() {
        if (this.editorContainer != null) return this.editorContainer;
        editorContainer = new JXTitledPanel(getMessage("main.propertieseditor.title.text"));
        editorContainer.setBorder(BorderFactory.createEmptyBorder());
        editorContainer.setContentContainer(getPropertiesEditor());
        return editorContainer;
    }

    private JettyControl getJettyControl() {
        if (this.jettyControl != null) return this.jettyControl;
        jettyControl = new JettyControl();
        return jettyControl;
    }

    public void log(String message) {
        getJettyControl().log(message);
    }

    public void toggleDisplayServerLog(boolean collapse) {
        getJettyControl().toggleDisplayServerLog(collapse);
    }

    public void onJettyStatusChange() {
        getPropertiesEditor().setFields(MockedFieldsRepository.getInstance().loadAll());
        getJettyControl().reloadContextList();
    }

}
