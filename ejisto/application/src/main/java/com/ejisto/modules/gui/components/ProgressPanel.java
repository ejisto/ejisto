/*******************************************************************************
 * Copyright 2010 Celestino Bellone
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

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
