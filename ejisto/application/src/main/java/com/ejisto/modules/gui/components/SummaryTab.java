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

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.Collection;

import javax.swing.JScrollPane;
import javax.swing.SwingConstants;

import org.jdesktop.swingx.JXLabel;
import org.jdesktop.swingx.JXPanel;

import com.ejisto.modules.dao.entities.MockedField;

public class SummaryTab extends JXPanel {
    private static final long serialVersionUID = 3654712395222166373L;
    private static final String MOCKED_FIELD_TEMPLATE = "<li>%s=%s</li>";
    private static final String CLASS_START           = "<div><b>%s</b>:<br/><ul>";
    private static final String NEXT_CLASS            = "</ul></div><div><b>%s</b>:<br/><ul>";
    private static final String FIELDS_END            = "</ul></div><br/></html>";
    private JXLabel summaryTextArea;
    private JScrollPane scrollPane;
    
    
    public SummaryTab() {
        super();
        initialize();
    }

    private void initialize() {
        setLayout(new BorderLayout());
        add(getScrollPane(), BorderLayout.CENTER);
    }
    
    private JScrollPane getScrollPane() {
        if(this.scrollPane != null) return this.scrollPane;
        scrollPane = new JScrollPane(getSummaryTextArea(), JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setPreferredSize(new Dimension(500,250));
        scrollPane.setMinimumSize(new Dimension(500,250));
        scrollPane.setMaximumSize(new Dimension(Short.MAX_VALUE,Short.MAX_VALUE));
        return scrollPane;
    }
    
    private JXLabel getSummaryTextArea() {
        if(this.summaryTextArea != null) return this.summaryTextArea;
        summaryTextArea = new JXLabel();
        summaryTextArea.setMinimumSize(new Dimension(450,200));
        summaryTextArea.setPreferredSize(new Dimension(450,200));
        summaryTextArea.setMaximumSize(new Dimension(Short.MAX_VALUE,Short.MAX_VALUE));
        summaryTextArea.setVerticalAlignment(SwingConstants.TOP);
        return summaryTextArea;
    }
    
    public void renderMockedFields(Collection<MockedField> fields) {
        String classname = null;
        StringBuffer buffer = new StringBuffer("<html>");
        for (MockedField mockedField : fields) {
            if(classname == null || !classname.equals(mockedField.getClassName())) {
                buffer.append(String.format(classname == null ? CLASS_START : NEXT_CLASS, mockedField.getClassName()));
                classname = mockedField.getClassName();                
            }
            renderMockedField(mockedField, buffer);
        }
        buffer.append(FIELDS_END);
        getSummaryTextArea().setText(buffer.toString());
    }
    
    private void renderMockedField(MockedField field, StringBuffer buffer) {
        buffer.append(String.format(MOCKED_FIELD_TEMPLATE, field.getFieldName(),field.getFieldValue()));
    }
}
