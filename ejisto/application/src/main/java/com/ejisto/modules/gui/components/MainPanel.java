/*******************************************************************************
 * Copyright 2010 Celestino Bellone
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package com.ejisto.modules.gui.components;

import static com.ejisto.util.GuiUtils.getMessage;
import static com.ejisto.util.SpringBridge.getAllMockedFields;

import java.awt.BorderLayout;
import java.awt.SystemColor;

import javax.swing.BorderFactory;

import org.jdesktop.swingx.JXPanel;
import org.jdesktop.swingx.JXTitledPanel;

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
    	if(header != null) return header;
    	header = new Header(getMessage("main.header.title"),getMessage("main.header.description"));
    	return header;
	}
    
    private JXPanel getWidgetsPane() {
        if(this.widgetsPane != null) return this.widgetsPane;
        widgetsPane = new JXPanel(new BorderLayout());
        widgetsPane.add(getEditorContainer(),BorderLayout.CENTER);
        widgetsPane.add(getJettyControl(), BorderLayout.SOUTH);
        return widgetsPane;
    }

    private MockedFieldsEditor getPropertiesEditor() {
        if(propertiesEditor != null) return propertiesEditor;
        propertiesEditor = new MockedFieldsEditor(true);
        propertiesEditor.setFields(getAllMockedFields());
        return propertiesEditor;
    }
    
    private JXTitledPanel getEditorContainer() {
    	if(this.editorContainer != null) return this.editorContainer;
    	editorContainer = new JXTitledPanel(getMessage("main.propertieseditor.title.text"));
    	editorContainer.setBorder(BorderFactory.createEmptyBorder());
    	editorContainer.setContentContainer(getPropertiesEditor());
    	return editorContainer;
    }
    
    private JettyControl getJettyControl() {
        if(this.jettyControl != null) return this.jettyControl;
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
    	getPropertiesEditor().setFields(getAllMockedFields());
    	getJettyControl().reloadContextList();
    }

}
