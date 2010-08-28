package com.ejisto.modules.gui.components;

import static com.ejisto.util.GuiUtils.getMessage;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.util.Collection;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;

import org.jdesktop.swingx.JXLabel;
import org.jdesktop.swingx.JXPanel;

import com.ejisto.modules.dao.entities.MockedField;
import com.ejisto.modules.gui.components.helper.Step;

public class ApplicationInstallerWizard extends JXPanel {

	private static final long serialVersionUID = 788313444893972067L;
	public static final String SELECT_FILE_COMMAND = "openSelectionDialog";
	private JXPanel content = null;
	private JXPanel fileSelectionPanel;
	private JXPanel cardContainer;
	private JXPanel fileSelectionTab;
	private ProgressPanel applicationScanningTab;
	private ProgressPanel fileExtractionTab;
	private ResourcesFilter classesFilteringTab;
	private MockedFieldsEditor propertiesEditingTab;
	private SummaryTab summaryTab;
	private CardLayout cardManager;
	private JXLabel selectedFilePath = null;
	private JButton fileSelection = null;

	public ApplicationInstallerWizard() {
		super();
		initialize();
	}
	
	public void initActions() {
		getFileSelection().setAction(getActionMap().get(SELECT_FILE_COMMAND));
	}
	
	public void goToStep(Step step) {
        cardManager.show(cardContainer, step.name());
    }

	public void previousStep(Step step) {
	    cardManager.show(cardContainer, step.name());
    }
	
	public void startProgress(String text, int numJobs) {
	    getApplicationScanningTab().initProgress(numJobs, text);
	}
	
	public void jobCompleted(String nextJob) {
	    getApplicationScanningTab().jobCompleted(nextJob);
	}
	
	public void fileExtractionCompleted(String message) {
	    getFileExtractionTab().jobCompleted(message);
	}
	
	private void initialize() {
        this.setLayout(new BorderLayout(0,0));
        this.setSize(new Dimension(600, 500));
        this.setMinimumSize(new Dimension(500, 400));
        this.add(getContent(), BorderLayout.CENTER);
	}
	
	/**
	 * This method initializes content	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JXPanel getContent() {
		if (content == null) {
			BorderLayout borderLayout = new BorderLayout();
			borderLayout.setHgap(20);
			borderLayout.setVgap(20);
			content = new JXPanel();
			content.setLayout(borderLayout);
			content.add(getCardContainer(), BorderLayout.CENTER);
		}
		return content;
	}
	
	private JXPanel getCardContainer() {
	    if(cardContainer != null) return cardContainer;
	    cardContainer = new JXPanel();
	    cardManager = new CardLayout();
	    
	    cardContainer.setLayout(cardManager);
	    cardContainer.add(getFileSelectionTab(), Step.FILE_SELECTION.name());
	    cardContainer.add(getFileExtractionTab(), Step.FILE_EXTRACTION.name());
	    cardContainer.add(getClassesFilteringTab(), Step.CLASSES_FILTERING.name());
	    cardContainer.add(getApplicationScanningTab(), Step.APPLICATION_SCANNING.name());
	    cardContainer.add(getPropertiesEditingTab(), Step.PROPERTIES_EDITING.name());
	    cardContainer.add(getSummaryTab(), Step.SUMMARY.name());
	    return cardContainer;
	}
	
	private JXPanel getFileSelectionTab() {
	    if(fileSelectionTab != null) return fileSelectionTab;
	    fileSelectionTab = new JXPanel(new FlowLayout());
	    fileSelectionTab.add(getFileSelectionPanel());
	    return fileSelectionTab;
	}
	
	private ProgressPanel getApplicationScanningTab() {
	    if(applicationScanningTab != null) return applicationScanningTab;
	    applicationScanningTab=new ProgressPanel();
	    return applicationScanningTab;
    }
	
	private ProgressPanel getFileExtractionTab() {
        if(fileExtractionTab != null) return fileExtractionTab;
        fileExtractionTab=new ProgressPanel();
        return fileExtractionTab;
    }
	
	private ResourcesFilter getClassesFilteringTab() {
	    if(classesFilteringTab != null) return classesFilteringTab;
	    classesFilteringTab = new ResourcesFilter();
	    return classesFilteringTab;
    }
	
	private MockedFieldsEditor getPropertiesEditingTab() {
	    if(propertiesEditingTab != null) return propertiesEditingTab;
	    propertiesEditingTab = new MockedFieldsEditor();
        return propertiesEditingTab;
    }
	
	private SummaryTab getSummaryTab() {
	    if(this.summaryTab != null) return this.summaryTab;
	    summaryTab = new SummaryTab();
        return summaryTab;
    }

	/**
	 * This method initializes fileSelectionPanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JXPanel getFileSelectionPanel() {
		if (fileSelectionPanel == null) {
		    fileSelectionPanel = new JXPanel();
			FlowLayout flowLayout = new FlowLayout();
			flowLayout.setAlignment(java.awt.FlowLayout.CENTER);
			flowLayout.setVgap(15);
			flowLayout.setHgap(15);
			fileSelectionPanel.setLayout(flowLayout);
			fileSelectionPanel.setMinimumSize(new Dimension(500, 50));
			fileSelectionPanel.setPreferredSize(new Dimension(500, 50));
			fileSelectionPanel.setBorder(BorderFactory.createEmptyBorder());
			fileSelectionPanel.add(getSelectedFilePath(), null);
			fileSelectionPanel.add(getFileSelection(), null);
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

	/**
	 * This method initializes fileSelection	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getFileSelection() {
		if (fileSelection == null) {
			fileSelection = new JButton();
			fileSelection.setText("...");
			fileSelection.setActionCommand(SELECT_FILE_COMMAND);
		}
		return fileSelection;
	}
	
	public void setMockedFields(List<MockedField> mockedFields) {
	    getPropertiesEditingTab().setFields(mockedFields);
	}

    public void setJars(List<String> includedJars) {
        getClassesFilteringTab().setResources(includedJars);
    }
    
    public List<String> getBlacklistedJars() {
        return getClassesFilteringTab().getBlacklistedObjects();
    }
    
    public void setSelectedFile(String selectedFile) {
        getSelectedFilePath().setText(selectedFile);
        getSelectedFilePath().setToolTipText(selectedFile);
    }
    
    public void setSummaryFields(Collection<MockedField> mockedFields) {
        getSummaryTab().renderMockedFields(mockedFields);
    }
    
	
}  //  @jve:decl-index=0:visual-constraint="10,10"
