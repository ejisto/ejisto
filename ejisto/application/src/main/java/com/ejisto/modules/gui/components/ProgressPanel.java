package com.ejisto.modules.gui.components;
import static com.ejisto.util.GuiUtils.getMessage;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JProgressBar;

import org.jdesktop.swingx.JXLabel;
import org.jdesktop.swingx.JXPanel;

public class ProgressPanel extends JXPanel {

    private static final long serialVersionUID = -2033285248036487856L;
    private String defaultMessage;
    private JProgressBar progress = null;
    private JXLabel title = null;
    private int jobsCompleted=0;

    /**
     * This method initializes 
     * 
     */
    public ProgressPanel() {
    	super();
    	initialize();
    }

    /**
     * This method initializes this
     * 
     */
    private void initialize() {
        this.defaultMessage=getMessage("progress.start");
        this.title = new JXLabel(defaultMessage, JXLabel.CENTER);
        this.title.setLineWrap(true);
        this.setLayout(new BorderLayout());
        this.setPreferredSize(new Dimension(500, 300));
        this.setMinimumSize(new Dimension(500, 300));
        this.setSize(new Dimension(500, 217));
        this.add(getProgress(), BorderLayout.SOUTH);
        this.add(title, BorderLayout.CENTER);
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
        jobsCompleted=0;
        title.setText(text);
        progress.setMaximum(jobs);
        progress.setValue(0);
        progress.setIndeterminate(false);
    }
    
    
    public void jobCompleted(String newText) {
        if(!progress.isIndeterminate())progress.setValue(++jobsCompleted);
        if(newText != null) title.setText(newText);
    }
    
    public void jobCompleted() {
        jobCompleted(null);
    }
    
    public void reset() {
        getProgress().setIndeterminate(true);
        title.setText(defaultMessage);
    }

}  //  @jve:decl-index=0:visual-constraint="10,10"
