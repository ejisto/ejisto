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

import com.ejisto.modules.dao.entities.MockedField;
import org.jdesktop.swingx.JXLabel;
import org.jdesktop.swingx.JXPanel;

import javax.swing.*;
import java.awt.*;
import java.util.Collection;
import java.util.stream.Collectors;

import static com.ejisto.modules.gui.components.EjistoDialog.DEFAULT_WIDTH;

public class SummaryTab extends JXPanel {
    private static final long serialVersionUID = 3654712395222166373L;
    private static final String MOCKED_FIELD_TEMPLATE = "<li>%s=%s</li>";
    private static final String CLASS_START = "<div><b>%s</b>:<br/><ul>";
    private static final String CLASS_END = "</ul></div>";
    private static final String FIELDS_END = "<br/></html>";
    private JXLabel summaryTextArea;
    private JScrollPane scrollPane;
    private JPanel buttonsPanel;

    public SummaryTab() {
        super();
        initialize();
    }

    private void initialize() {
        setLayout(new BorderLayout());
        add(getScrollPane(), BorderLayout.CENTER);
    }

    private JScrollPane getScrollPane() {
        if (this.scrollPane != null) {
            return this.scrollPane;
        }
        scrollPane = new JScrollPane(getSummaryTextArea(), JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                                     JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setPreferredSize(new Dimension(DEFAULT_WIDTH, 250));
        scrollPane.setMinimumSize(new Dimension(DEFAULT_WIDTH, 250));
        scrollPane.setMaximumSize(new Dimension(Short.MAX_VALUE, Short.MAX_VALUE));
        return scrollPane;
    }

    private JXLabel getSummaryTextArea() {
        if (this.summaryTextArea != null) {
            return this.summaryTextArea;
        }
        summaryTextArea = new JXLabel();
        summaryTextArea.setMinimumSize(new Dimension(450, 200));
        summaryTextArea.setPreferredSize(new Dimension(450, 200));
        summaryTextArea.setMaximumSize(new Dimension(Short.MAX_VALUE, Short.MAX_VALUE));
        summaryTextArea.setVerticalAlignment(SwingConstants.TOP);
        return summaryTextArea;
    }

    public void renderMockedFields(Collection<MockedField> fields) {
        StringBuilder buffer = new StringBuilder("<html>");
        fields.stream()
                .collect(Collectors.groupingBy(MockedField::getClassName))
                .entrySet()
                .stream()
                .forEach(entry -> {
                    buffer.append(String.format(CLASS_START, entry.getKey()));
                    entry.getValue().forEach(field -> renderMockedField(field, buffer));
                    buffer.append(CLASS_END);
                });
        buffer.append(FIELDS_END);
        getSummaryTextArea().setText(buffer.toString());
    }

    private void renderMockedField(MockedField field, StringBuilder buffer) {
        buffer.append(String.format(MOCKED_FIELD_TEMPLATE, field.getFieldName(),
                                    field.isSimpleValue() ? field.getFieldValue() : "**expression**"));
    }
}
