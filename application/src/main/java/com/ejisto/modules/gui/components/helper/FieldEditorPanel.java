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

package com.ejisto.modules.gui.components.helper;

import com.ejisto.modules.controller.MockedFieldCreationController;
import com.ejisto.modules.validation.NumberValidator;
import com.ejisto.util.GuiUtils;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import org.jdesktop.swingx.JXCollapsiblePane;
import org.jdesktop.swingx.JXLabel;

import javax.swing.*;
import javax.swing.plaf.basic.BasicComboBoxRenderer;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.Collection;

import static com.ejisto.modules.controller.MockedFieldsEditorController.CANCEL_EDITING;
import static com.ejisto.modules.controller.MockedFieldsEditorController.STOP_EDITING;
import static com.ejisto.util.GuiUtils.getMessage;

/**
 * Created by IntelliJ IDEA.
 * User: celestino
 * Date: 6/6/12
 * Time: 6:28 PM
 */
public final class FieldEditorPanel extends JXCollapsiblePane implements ActionListener {
    private static final String TYPE_SELECTION = "typeSelection";

    private JXLabel type;
    private JComboBox<TypeEntry> typeSelector;
    private JButton ok;
    private JButton cancel;
    private JPanel editor;
    private JXLabel additionalInfoTitle;
    private JFormattedTextField additionalInfo;
    private String expression;
    private String fieldClass;
    private String fieldSize;
    private FieldsEditorContext editorContext;
    private JTextField value;
    private JComboBox<String> contextPathSelector;
    private JTextField fieldType;

    public FieldEditorPanel(ActionMap actionMap, FieldsEditorContext editorContext) {
        this.editorContext = editorContext;
        GuiUtils.fillActionMap(getActionMap(), actionMap);
        if (editorContext.getComplexEditorRows() > 0) {
            init();
        }
    }

    public void setFocusOnFirstField() {
        if (typeSelector != null) {
            typeSelector.requestFocus();
        }
    }

    public void setTypes(Collection<String> types) {
        this.typeSelector.removeAllItems();
        types.stream().map(TypeEntry::fromType).forEach(typeSelector::addItem);
        this.fieldClass = getSelectedType();
    }

    public void setTitle(String title) {
        getEditor().setBorder(BorderFactory.createTitledBorder(title));
    }

    JPanel getEditor() {
        return editor;
    }

    public String getFieldClass() {
        return fieldClass;
    }

    public String getExpression() {
        return expression;
    }

    public String getFieldSize() {
        return fieldSize;
    }

    public String getFieldName() {
        return additionalInfo.getText();
    }

    public String getFieldValue() {
        return value.getText();
    }

    public void setAvailableContextPaths(Collection<String> availableContextPaths) {
        contextPathSelector.removeAllItems();
        availableContextPaths.forEach(contextPathSelector::addItem);
    }

    public String getContextPath() {
        return (String) contextPathSelector.getSelectedItem();
    }

    public String getFieldType() {
        return fieldType.getText();
    }

    void onTypeSelected() {
        fieldClass = getSelectedType();
        if (fieldClass != null) {
            typeSelector.setToolTipText(fieldClass);
        }
    }

    private String getSelectedType() {
        if (typeSelector == null || typeSelector.getSelectedItem() == null) {
            return null;
        }
        return ((TypeEntry) typeSelector.getSelectedItem()).type;
    }

    void setSize(String fieldSize) {
        this.fieldSize = fieldSize;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Action action = getActionMap().get(e.getActionCommand());
        if (action != null) {
            action.actionPerformed(e);
        }
    }

    static String abbreviate(String type) {
        String[] elements = type.split("\\.");
        int length = elements.length;
        if (length < 4) {
            return type;
        }
        StringBuilder out = new StringBuilder("[..]");
        for (int i = length - 2; i < length; i++) {
            out.append(".").append(elements[i]);
        }
        return out.toString();
    }

    private String getColumnLayout() {
        return "fill:max(d;4px):grow,left:4dlu:grow,10px,left:pref:nogrow,fill:max(d;4px):grow";
    }

    private String getRowsLayout() {
        StringBuilder rowsLayout = new StringBuilder();
        for (int row = 0; row < editorContext.getComplexEditorRows(); row++) {
            rowsLayout.append("pref,2dlu").append(",");
        }
        rowsLayout.append("pref");
        return rowsLayout.toString();
    }

    private void init() {
        createUIComponents();
        FormLayout editorLayout = new FormLayout(getColumnLayout(), getRowsLayout());
        editorLayout.setRowGroups(getRowGroups());
        editor.setLayout(editorLayout);
        CellConstraints cc = new CellConstraints();
        editor.add(type, cc.xy(2, 1));
        editor.add(typeSelector, cc.xy(4, 1));
        editor.add(additionalInfoTitle, cc.xy(2, 3));
        editor.add(additionalInfo, cc.xy(4, 3));
        addAdditionalFields(cc);
        JPanel buttonsPanel = new JPanel();
        buttonsPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
        if (ok != null) {
            buttonsPanel.add(ok);
        }
        if (cancel != null) {
            buttonsPanel.add(cancel);
        }
        editor.add(buttonsPanel, cc.xyw(2, editorContext.getComplexEditorRows() * 2 + 1, 3));
        type.setLabelFor(typeSelector);
    }

    private void addAdditionalFields(CellConstraints cc) {
        if (editorContext != FieldsEditorContext.CREATE_FIELD) {
            return;
        }
        JXLabel valueTitle = new JXLabel(getMessage("create.new.field.value"));
        value = new JTextField();
        value.setPreferredSize(new Dimension(200, 20));
        value.setMaximumSize(new Dimension(200, 20));
        contextPathSelector = new JComboBox<>();
        contextPathSelector.addActionListener(this);
        contextPathSelector.setActionCommand(MockedFieldCreationController.CTX_SELECTION);
        fieldType = new JTextField("java.lang.String");
        fieldType.setPreferredSize(new Dimension(200, 20));
        fieldType.setMaximumSize(new Dimension(200, 20));
        editor.add(valueTitle, cc.xy(2, 5));
        editor.add(value, cc.xy(4, 5));
        editor.add(new JXLabel(getMessage("create.new.field.type")), cc.xy(2, 7));
        editor.add(fieldType, cc.xy(4, 7));
        editor.add(new JXLabel(getMessage("create.new.field.context.path")), cc.xy(2, 9));
        editor.add(contextPathSelector, cc.xy(4, 9));

    }

    private void createUIComponents() {
        setLayout(new BorderLayout());
        editor = new JPanel();
        editor.setPreferredSize(new Dimension(200, 150));
        type = new JXLabel(getMessage(getComboBoxLabel()));
        typeSelector = new JComboBox<>();
        typeSelector.setAction(new CallbackAction("type", e -> onTypeSelected()));
        typeSelector.setRenderer(new TypeEntryRenderer());
        typeSelector.setActionCommand(TYPE_SELECTION);
        additionalInfoTitle = new JXLabel(getMessage(getAdditionalInfoLabel()));
        additionalInfo = new JFormattedTextField();
        configureAdditionalInfoField(additionalInfo);
        additionalInfo.setPreferredSize(new Dimension(getAdditionalInfoWidth(), 20));
        additionalInfo.setMaximumSize(new Dimension(getAdditionalInfoWidth(), 20));
        registerKeyboardAction(getActionMap().get(STOP_EDITING), STOP_EDITING,
                               KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        addPropertyChangeListener("collapsed", evt -> {
            if (!((Boolean) evt.getNewValue())) {
                setFocusOnFirstField();
            }
        });
        if (getActionMap().get(STOP_EDITING) != null) {
            ok = new JButton(getActionMap().get(STOP_EDITING));
        }
        if (getActionMap().get(CANCEL_EDITING) != null) {
            cancel = new JButton(getActionMap().get(CANCEL_EDITING));
        }
        add(editor, BorderLayout.CENTER);
    }

    private int getAdditionalInfoWidth() {
        return isCreateFieldContext() ? 200 : 30;
    }

    private boolean isCreateFieldContext() {
        return editorContext == FieldsEditorContext.CREATE_FIELD;
    }

    private String getComboBoxLabel() {
        return isCreateFieldContext() ? "create.new.field.class" : "wizard.properties.editor.complex.type";
    }

    private String getAdditionalInfoLabel() {
        return isCreateFieldContext() ? "create.new.field.name" : "wizard.properties.editor.complex.size";
    }

    private void configureAdditionalInfoField(JFormattedTextField additionalInfo) {

        if (!isCreateFieldContext()) {
            additionalInfo.setInputVerifier(new NumberValidator(NumberValidator.ValidationType.SIGNED_INTEGER));
            additionalInfo.setValue("10");
            additionalInfo.addPropertyChangeListener("value", new ClosurePropertyChangeListener("", this::setSize));
        }
        additionalInfo.addFocusListener(new TextComponentFocusListener());
    }

    private int[][] getRowGroups() {
        int complexEditorRows = editorContext.getComplexEditorRows();
        int[][] rowGroups = new int[1][complexEditorRows];
        for (int i = 1, index = 0; i < complexEditorRows * 2; i++) {
            if (i % 2 > 0) {
                rowGroups[0][index++] = i;
            }
        }
        return rowGroups;
    }

    static final class TypeEntry {

        final String description;
        final String type;

        TypeEntry(String type, String description) {
            this.type = type;
            this.description = description;
        }

        @Override
        public String toString() {
            return description;
        }

        static TypeEntry fromType(String type) {
            return new TypeEntry(type, abbreviate(type));
        }
    }

    private static final class TypeEntryRenderer implements ListCellRenderer<TypeEntry> {

        private final BasicComboBoxRenderer helper = new BasicComboBoxRenderer();

        private TypeEntryRenderer() {
        }

        @Override
        public Component getListCellRendererComponent(JList<? extends TypeEntry> list, TypeEntry value, int index, boolean isSelected, boolean cellHasFocus) {
            Component component = helper.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (value != null) {
                ((JComponent) component).setToolTipText(value.type);
            }
            return component;
        }
    }
}
