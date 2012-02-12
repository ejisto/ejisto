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

package com.ejisto.modules.gui.components;

import com.ejisto.modules.executor.ErrorDescriptor;
import org.jdesktop.swingx.JXCollapsiblePane;
import org.jdesktop.swingx.JXLabel;
import org.jdesktop.swingx.JXPanel;
import org.jdesktop.swingx.JXTable;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import static com.ejisto.util.GuiUtils.getErrorIcon;
import static com.ejisto.util.GuiUtils.getMessage;

public class ProgressPanel extends JXPanel {

    private static final long serialVersionUID = -2033285248036487856L;
    private String defaultMessage;
    private JProgressBar progress = null;
    private JXLabel title = null;
    private int jobsCompleted = 0;
    private JXCollapsiblePane collapsiblePane;
    private JXTable eventTable;
    private final List<ErrorDescriptor> errors = new ArrayList<ErrorDescriptor>();

    /**
     * This method initializes
     */
    public ProgressPanel() {
        super();
        initialize();
    }

    /**
     * This method initializes this
     */
    private void initialize() {
        this.defaultMessage = getMessage("progress.start");
        this.title = new JXLabel(defaultMessage, JXLabel.CENTER);
        this.title.setLineWrap(true);
        this.setLayout(new BorderLayout());
        this.setPreferredSize(new Dimension(500, 300));
        this.setMinimumSize(new Dimension(500, 300));
        this.setSize(new Dimension(500, 217));
        this.add(getProgress(), BorderLayout.SOUTH);
        this.add(title, BorderLayout.CENTER);
        this.add(getCollapsiblePane(), BorderLayout.NORTH);
    }

    /**
     * This method initializes progress
     *
     * @return javax.swing.JProgressBar
     */
    private JProgressBar getProgress() {
        if (progress == null) {
            progress = new JProgressBar();
            progress.setIndeterminate(true);
        }
        return progress;
    }

    public void initProgress(int jobs, String text) {
        jobsCompleted = 0;
        title.setText(text);
        progress.setMaximum(jobs);
        progress.setValue(0);
        progress.setIndeterminate(false);
    }

    public void jobCompleted(String newText) {
        if (!progress.isIndeterminate()) progress.setValue(++jobsCompleted);
        if (newText != null) title.setText(newText);
    }

    public void jobCompleted() {
        jobCompleted(null);
    }

    public void reset() {
        getProgress().setIndeterminate(true);
        title.setText(defaultMessage);
    }

    public void processCompleted(String text) {
        progress.setIndeterminate(false);
        progress.setMaximum(1);
        progress.setMinimum(0);
        jobCompleted(text);
    }

    public void addError(ErrorDescriptor errorDescriptor) {
        errorTableModel.addRow(errorDescriptor);
        getEventTable().setModel(errorTableModel);
        getEventTable().setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
        getEventTable().packColumn(0, 2);
        getEventTable().getColumn(0).setPreferredWidth(50);
        getEventTable().packColumn(1, -1);
        if (getCollapsiblePane().isCollapsed())
            getCollapsiblePane().setCollapsed(false);
    }

    private JXCollapsiblePane getCollapsiblePane() {
        if (collapsiblePane != null) return collapsiblePane;
        collapsiblePane = new JXCollapsiblePane();
        collapsiblePane.setCollapsed(true);
        JScrollPane scrollPane = new JScrollPane(getEventTable());
        scrollPane.setPreferredSize(new Dimension(100, 100));
        scrollPane.setMaximumSize(new Dimension(Short.MAX_VALUE, 100));
        collapsiblePane.getContentPane().add(scrollPane, BorderLayout.CENTER);
        return collapsiblePane;
    }

    private JXTable getEventTable() {
        if (eventTable != null) return eventTable;
        eventTable = new JXTable();
        return eventTable;
    }

    private final static ErrorTableModel errorTableModel = new ErrorTableModel();

    private static final class ErrorTableModel extends AbstractTableModel {

        private final List<ErrorDescriptor> data;
        private static final String[] COLUMNS = {"Severity", "Error"};

        public ErrorTableModel() {
            super();
            this.data = new ArrayList<ErrorDescriptor>();

        }

        public void addRow(ErrorDescriptor errorDescriptor) {
            this.data.add(errorDescriptor);
            fireTableDataChanged();
        }

        @Override
        public int getRowCount() {
            return data.size();
        }

        @Override
        public int getColumnCount() {
            return COLUMNS.length;
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            ErrorDescriptor value = data.get(rowIndex);
            return columnIndex == 0 ? getErrorIcon(value.getCategory()) : value.getErrorDescription();
        }

        @Override
        public String getColumnName(int column) {
            return COLUMNS[column];
        }

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            if (columnIndex == 0) return Icon.class;
            return String.class;
        }
    }
}
