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

package com.ejisto.modules.controller.wizard.installer;

import ch.lambdaj.function.closure.Closure1;
import com.ejisto.modules.gui.components.EjistoDialog;
import com.ejisto.modules.gui.components.helper.CallbackAction;
import com.ejisto.modules.gui.components.helper.Step;
import com.ejisto.modules.repository.SettingsRepository;
import org.jdesktop.swingx.JXLabel;
import org.jdesktop.swingx.JXPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;

import static ch.lambdaj.Lambda.var;
import static com.ejisto.constants.StringConstants.LAST_FILESELECTION_PATH;
import static com.ejisto.constants.StringConstants.SELECT_FILE_COMMAND;
import static com.ejisto.util.GuiUtils.getMessage;
import static com.ejisto.util.GuiUtils.selectFile;

public class FileSelectionController extends AbstractApplicationInstallerController {
    private JXPanel fileSelectionTab;
    private JXPanel fileSelectionPanel;
    private JXLabel selectedFilePath;
    private JButton fileSelection;
    private File selectedFile;
    private boolean executionCompleted;
    private Closure1<ActionEvent> callActionPerformed;

    public FileSelectionController(EjistoDialog dialog) {
        super(dialog);
    }

    @Override
    public JPanel getView() {
        callActionPerformed = new Closure1<ActionEvent>() {{
            of(FileSelectionController.this).actionPerformed(var(ActionEvent.class));
        }};
        return getFileSelectionTab();
    }

    @Override
    public boolean canProceed() {
        return true;
    }

    @Override
    public boolean isExecutionSucceeded() {
        return selectedFile != null;
    }

    @Override
    public Step getStep() {
        return Step.FILE_SELECTION;
    }

    public void actionPerformed(ActionEvent e) {
        if (SELECT_FILE_COMMAND.getValue().equals(e.getActionCommand())) {
            selectedFile = openFileSelectionDialog();
            if (selectedFile != null) {
                getSelectedFilePath().setText(selectedFile.getAbsolutePath());
                getSelectedFilePath().setToolTipText(selectedFile.getAbsolutePath());
            }
            executionCompleted = true;
        }
    }

    @Override
    public void activate() {
        //no extra work here :)
    }

    private File openFileSelectionDialog() {
        return selectFile(getDialog(), SettingsRepository.getInstance().getSettingValue(LAST_FILESELECTION_PATH), true);
    }

    private JXPanel getFileSelectionTab() {
        if (fileSelectionTab != null) return fileSelectionTab;
        fileSelectionTab = new JXPanel(new BorderLayout());
        JXPanel spacer = new JXPanel();
        spacer.setPreferredSize(new Dimension(500, 100));
        fileSelectionTab.add(spacer, BorderLayout.NORTH);
        fileSelectionTab.add(getFileSelectionPanel(), BorderLayout.CENTER);
        return fileSelectionTab;
    }

    private JXPanel getFileSelectionPanel() {
        if (this.fileSelectionPanel == null) {
            fileSelectionPanel = new JXPanel();
            FlowLayout flowLayout = new FlowLayout();
            flowLayout.setAlignment(java.awt.FlowLayout.CENTER);
            flowLayout.setVgap(15);
            flowLayout.setHgap(15);
            fileSelectionPanel.setLayout(flowLayout);
            fileSelectionPanel.setMinimumSize(new Dimension(500, 50));
            fileSelectionPanel.setPreferredSize(new Dimension(500, 50));
            fileSelectionPanel.add(getSelectedFilePath());
            fileSelectionPanel.add(getFileSelection());
        }
        return fileSelectionPanel;
    }

    private JXLabel getSelectedFilePath() {
        if (selectedFilePath != null) return selectedFilePath;
        selectedFilePath = new JXLabel(getMessage("wizard.file.selected.default.text"));
        selectedFilePath.setFont(new Font("Helvetica", Font.PLAIN, 10));
        selectedFilePath.setMaximumSize(new Dimension(300, 25));
        selectedFilePath.setMinimumSize(new Dimension(300, 25));
        selectedFilePath.setBorder(BorderFactory.createLineBorder(Color.gray, 1));
        selectedFilePath.setPreferredSize(new Dimension(300, 25));
        return selectedFilePath;
    }

    private JButton getFileSelection() {
        if (fileSelection == null) {
            fileSelection = new JButton(new CallbackAction("...", SELECT_FILE_COMMAND.getValue(), callActionPerformed));
        }
        return fileSelection;
    }

    @Override
    public void beforeNext() {
        getSession().setWarFile(selectedFile);
    }

    @Override
    public String getTitleKey() {
        return "wizard.fileselection.title";
    }

    @Override
    public String getDescriptionKey() {
        return "wizard.fileselection.description";
    }

}
