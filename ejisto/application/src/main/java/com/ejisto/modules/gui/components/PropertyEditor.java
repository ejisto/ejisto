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

import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import java.awt.*;

public class PropertyEditor extends JXPanel implements TableModelListener {
    private static final long serialVersionUID = 1619044790610314913L;
    private JScrollPane table;
    private JXPanel buttons;

    public PropertyEditor() {
        initLayout();
        initComponents();
    }

    private void initComponents() {
        add(getTable(), BorderLayout.CENTER);
        add(getButtonsBar(), BorderLayout.SOUTH);
    }

    private JScrollPane getTable() {
        if (table != null) return table;
        MockedFieldsTableModel tm = new MockedFieldsTableModel(MockedFieldsRepository.getInstance().loadAll());
        JTable data = new JTable(tm);
        table = new JScrollPane(data, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        return table;
    }

    private JXPanel getButtonsBar() {
        if (buttons != null) return buttons;
        buttons = new JXPanel();
        buttons.setLayout(new FlowLayout());
        buttons.setMinimumSize(new Dimension(200, 30));
        buttons.setMaximumSize(new Dimension(Short.MAX_VALUE, 30));
        buttons.setPreferredSize(new Dimension(200, 30));
        buttons.add(new JButton("Insert"));
        buttons.add(new JButton("Update"));
        buttons.add(new JButton("Delete"));
        return buttons;
    }

    private void initLayout() {
        setLayout(new BorderLayout(10, 10));
    }

    @Override
    public void tableChanged(TableModelEvent e) {

    }


}
