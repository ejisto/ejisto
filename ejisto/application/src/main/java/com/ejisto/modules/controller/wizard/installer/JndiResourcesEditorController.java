/*
 * Ejisto, a powerful developer assistant
 *
 * Copyright (C) 2010-2011  Celestino Bellone
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

import com.ejisto.constants.StringConstants;
import com.ejisto.modules.controller.WizardException;
import com.ejisto.modules.dao.entities.JndiDataSource;
import com.ejisto.modules.gui.components.EjistoDialog;
import com.ejisto.modules.gui.components.JndiResourcesEditor;
import com.ejisto.modules.gui.components.helper.BoundResourceEditor;
import com.ejisto.modules.gui.components.helper.Step;
import com.ejisto.modules.validation.DataSourceEnvEntryValidator;
import com.ejisto.modules.validation.ValidationErrors;
import org.springframework.util.CollectionUtils;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.List;

import static com.ejisto.constants.StringConstants.SELECT_FILE_COMMAND;
import static com.ejisto.util.IOUtils.copyFile;
import static com.ejisto.util.JndiDataSourcesRepository.update;

public class JndiResourcesEditorController extends AbstractApplicationInstallerController {
    private JndiResourcesEditor jndiResourcesEditor;

    public JndiResourcesEditorController(EjistoDialog dialog) {
        super(dialog);
    }

    @Override
    public JndiResourcesEditor getView() {
        if (jndiResourcesEditor != null) return jndiResourcesEditor;
        jndiResourcesEditor = new JndiResourcesEditor();
        jndiResourcesEditor.setFileSelectionListener(this);
        return jndiResourcesEditor;
    }

    @Override
    public boolean canProceed() {
        return getSession().containsDataSources();
    }

    @Override
    public boolean isExecutionSucceeded() throws WizardException {
        return true;
    }

    @Override
    public Step getStep() {
        return Step.JNDI_RESOURCES_EDITOR;
    }

    @Override
    public void activate() {
        getView().init(getSession().getDataSources());
    }

    @Override
    public boolean executionCompleted() {
        return true;
    }

    @Override
    public String getTitleKey() {
        return "wizard.jndiresourceseditor.title";
    }

    @Override
    public String getDescriptionKey() {
        return "wizard.jndiresourceseditor.description";
    }

    @Override
    public boolean isBackEnabled() {
        return getSession().containsDataSources();
    }

    @Override
    public boolean isForwardEnabled() {
        return getSession().containsDataSources();
    }

    @Override
    public boolean validateInput() {
        List<JndiDataSource> entries = getView().getDataSources();
        if (CollectionUtils.isEmpty(entries)) return true;
        DataSourceEnvEntryValidator validator = new DataSourceEnvEntryValidator();
        ValidationErrors errors = new ValidationErrors("JndiDataSource");
        validator.validateAll(entries, errors);
        return !errors.hasErrors();
    }

    @Override
    public void beforeNext() {
        File libDir = new File(System.getProperty(StringConstants.LIB_DIR.getValue()));
        for (JndiDataSource dataSource : getView().getDataSources()) {
            dataSource.setDriverJarPath(copyFile(dataSource.getDriverJarPath(), libDir));
            update(dataSource);
        }
    }

    public void actionPerformed(ActionEvent event) {
        if (SELECT_FILE_COMMAND.getValue().equals(event.getActionCommand())) {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setFileFilter(new FileNameExtensionFilter("*.jar", "jar"));
            if (fileChooser.showOpenDialog(getDialog()) == JFileChooser.APPROVE_OPTION) {
                ((BoundResourceEditor) event.getSource()).setJarFilePath(
                        fileChooser.getSelectedFile().getAbsolutePath());
            }
        }
    }
}
