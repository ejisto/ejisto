/*
 * Ejisto, a powerful developer assistant
 *
 * Copyright (C) 2010-2012  Celestino Bellone
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

import ch.lambdaj.function.closure.Closure0;
import ch.lambdaj.function.closure.Closure1;
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
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collection;

import static ch.lambdaj.Lambda.var;
import static com.ejisto.modules.controller.MockedFieldsEditorController.CANCEL_EDITING;
import static com.ejisto.modules.controller.MockedFieldsEditorController.STOP_EDITING;
import static com.ejisto.util.GuiUtils.getMessage;

/**
 * Created by IntelliJ IDEA.
 * User: celestino
 * Date: 12/19/10
 * Time: 1:47 PM
 */
public class MockedFieldValueEditorPanel extends JXCollapsiblePane implements ActionListener {
    private static final String TYPE_SELECTION = "typeSelection";
    private JXLabel type;
    private JComboBox genericType;
    private JButton ok;
    private JButton cancel;
    private JPanel editor;
    private JPanel buttonsPanel;
    private JXLabel size;
    private JFormattedTextField collectionSize;
    private String expression;
    private String fieldType;
    private String fieldSize;

    public MockedFieldValueEditorPanel(ActionMap actionMap) {
        GuiUtils.fillActionMap(getActionMap(), actionMap);
        $$$setupUI$$$();
    }

    public void setFocusOnFirstField() {
        if (genericType != null) {
            genericType.requestFocus();
        }
    }

    public void setTypes(Collection<String> types) {
        this.genericType.removeAllItems();
        for (String s : types) {
            this.genericType.addItem(TypeEntry.fromType(s));
        }
        this.fieldType = getSelectedType();
    }

    public void setTitle(String title) {
        getEditor().setBorder(BorderFactory.createTitledBorder(title));
    }

    public JPanel getEditor() {
        return editor;
    }

    public String getFieldType() {
        return fieldType;
    }

    public String getExpression() {
        return expression;
    }

    public String getFieldSize() {
        return fieldSize;
    }

    private void createUIComponents() {
        setLayout(new BorderLayout());
        editor = new JPanel();
        editor.setPreferredSize(new Dimension(200, 150));
        type = new JXLabel(getMessage("wizard.properties.editor.complex.type"));
        genericType = new JComboBox();
        genericType.setAction(new CallbackAction("type", new Closure0() {{
            of(MockedFieldValueEditorPanel.this).onTypeSelected();
        }}));
        genericType.setRenderer(new BasicComboBoxRenderer() {
            @Override
            public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                Component component = super.getListCellRendererComponent(list, value, index, isSelected,
                                                                         cellHasFocus);
                ((JComponent) component).setToolTipText(((TypeEntry) value).type);
                return component;
            }
        });
        genericType.setActionCommand(TYPE_SELECTION);
        ok = new JButton(getActionMap().get(STOP_EDITING));
        //ok.setActionCommand(STOP_EDITING);
        //ok.addActionListener(this);
        cancel = new JButton(getActionMap().get(CANCEL_EDITING));
        //cancel.setActionCommand(CANCEL_EDITING);
        //cancel.addActionListener(this);
        size = new JXLabel(getMessage("wizard.properties.editor.complex.size"));
        collectionSize = new JFormattedTextField();
        collectionSize.addPropertyChangeListener("value",
                                                 new ClosurePropertyChangeListener("", new Closure1<String>() {{
                                                     of(MockedFieldValueEditorPanel.this).setSize(var(String.class));
                                                 }}, null));
        collectionSize.setInputVerifier(new NumberValidator(NumberValidator.ValidationType.SIGNED_INTEGER));
        collectionSize.addFocusListener(new TextComponentFocusListener());
        collectionSize.setValue("10");
        add(editor, BorderLayout.CENTER);
        registerKeyboardAction(getActionMap().get(STOP_EDITING), STOP_EDITING,
                               KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        addPropertyChangeListener("collapsed", new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if (!((Boolean) evt.getNewValue())) {
                    type.grabFocus();
                }
            }
        });
    }

    public void onTypeSelected() {
        fieldType = getSelectedType();
        if (fieldType != null) {
            genericType.setToolTipText(fieldType);
        }
    }

    private String getSelectedType() {
        if (genericType == null || genericType.getSelectedItem() == null) {
            return null;
        }
        return ((TypeEntry) genericType.getSelectedItem()).type;
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

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        createUIComponents();
        editor.setLayout(new FormLayout(
                "fill:max(d;4px):grow,left:4dlu:noGrow,fill:max(p;60px):grow,left:4dlu:noGrow,fill:216px:noGrow,left:9px:grow,fill:max(d;4px):noGrow",
                "center:max(d;4px):noGrow,top:4dlu:noGrow,center:max(d;4px):noGrow,top:5dlu:noGrow,center:max(m;4px):noGrow,top:4dlu:noGrow,center:max(d;4px):noGrow,top:4dlu:noGrow,center:max(d;4px):noGrow,top:4dlu:noGrow,center:max(d;4px):noGrow"));
        CellConstraints cc = new CellConstraints();
        editor.add(type, cc.xy(3, 3));
        editor.add(genericType, cc.xy(5, 3));
        buttonsPanel = new JPanel();
        buttonsPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
        editor.add(buttonsPanel, cc.xyw(3, 9, 3));
        buttonsPanel.add(ok);
        buttonsPanel.add(cancel);
        editor.add(size, cc.xy(3, 5));
        editor.add(collectionSize, cc.xy(5, 5, CellConstraints.FILL, CellConstraints.DEFAULT));
        type.setLabelFor(genericType);
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() { return editor; }

    private static final class TypeEntry {

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
}
