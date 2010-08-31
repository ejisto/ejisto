package com.ejisto.modules.controller.wizard.installer;

import static ch.lambdaj.Lambda.var;
import static com.ejisto.constants.StringConstants.SELECT_FILE_COMMAND;
import static com.ejisto.util.GuiUtils.getMessage;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.filechooser.FileFilter;

import org.jdesktop.swingx.JXLabel;
import org.jdesktop.swingx.JXPanel;

import ch.lambdaj.function.closure.Closure1;

import com.ejisto.modules.gui.components.EjistoDialog;
import com.ejisto.modules.gui.components.helper.CallbackAction;
import com.ejisto.modules.gui.components.helper.Step;

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
    	callActionPerformed = new Closure1<ActionEvent>() {{ of(FileSelectionController.this).actionPerformed(var(ActionEvent.class)); }};
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
        if(SELECT_FILE_COMMAND.getValue().equals(e.getActionCommand())) {
            selectedFile = openFileSelectionDialog();
            if(selectedFile != null) {
            	getSelectedFilePath().setText(selectedFile.getAbsolutePath());
            	getSelectedFilePath().setToolTipText(selectedFile.getAbsolutePath());
            }
            executionCompleted=true;
        }
    }
    
    @Override
    public void activate() {
        //no extra work here :)
    }

    @Override
    public boolean executionCompleted() {
        return executionCompleted;
    }

    
    private File openFileSelectionDialog() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new FileFilter() {
            @Override
            public boolean accept(File f) {
                return f.isDirectory() || f.getName().endsWith(".war");
            }

            @Override
            public String getDescription() {
                return "*.war";
            }
        });
        if (fileChooser.showOpenDialog(getDialog()) == JFileChooser.APPROVE_OPTION)
            return fileChooser.getSelectedFile();
        else
            return null;
    }
    
    private JXPanel getFileSelectionTab() {
        if(fileSelectionTab != null) return fileSelectionTab;
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
        if(selectedFilePath != null) return selectedFilePath;
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
