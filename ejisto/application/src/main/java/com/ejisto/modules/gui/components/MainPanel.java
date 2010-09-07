package com.ejisto.modules.gui.components;

import static com.ejisto.util.GuiUtils.getMessage;
import static com.ejisto.util.SpringBridge.getAllMockedFields;

import java.awt.BorderLayout;
import java.awt.SystemColor;

import org.jdesktop.swingx.JXPanel;
import org.jdesktop.swingx.JXTitledPanel;

public class MainPanel extends JXPanel {
    private static final long serialVersionUID = -28148619997853619L;

//    private JXTaskPaneContainer taskPaneContainer;
    private LogViewer logViewer;
//    private JSplitPane splitPane;

    private MockedFieldsEditor propertiesEditor;

	private Header header;

	private JXTitledPanel editorContainer;


    private JXPanel widgetsPane;

    public MainPanel() {
        super();
        init();
    }

    private void init() {
        initLayout();
        initComponents();
    }

    private void initLayout() {
        setLayout(new BorderLayout(5,5));
    }

    private void initComponents() {
    	setBackground(SystemColor.control);
    	add(getHeader(), BorderLayout.NORTH);
        add(getWidgetsPane(), BorderLayout.CENTER);
    }
    
    private Header getHeader() {
    	if(header != null) return header;
    	header = new Header(getMessage("main.header.title"),getMessage("main.header.description"));
    	return header;
	}
    
    private JXPanel getWidgetsPane() {
        if(this.widgetsPane != null) return this.widgetsPane;
        widgetsPane = new JXPanel(new BorderLayout());
        widgetsPane.add(getEditorContainer(),BorderLayout.CENTER);
        widgetsPane.add(getLogViewer(), BorderLayout.SOUTH);
        return widgetsPane;
    }

//	private JSplitPane getSplitPane() {
//        if(splitPane != null) return splitPane;
//        splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, getTaskPaneContainer(), getEditorContainer());
//        splitPane.setOneTouchExpandable(true);
//        splitPane.setBackground(SystemColor.control);
//        return splitPane;
//    }

//    private JXTaskPaneContainer getTaskPaneContainer() {
//        if (taskPaneContainer != null)
//            return taskPaneContainer;
//        taskPaneContainer = new JXTaskPaneContainer();
//        taskPaneContainer.setMinimumSize(new Dimension(200,200));
//        taskPaneContainer.setPreferredSize(new Dimension(200,200));
//        taskPaneContainer.setMaximumSize(new Dimension(200, Short.MAX_VALUE));
//        taskPaneContainer.setBackground(SystemColor.control);
//        JXTaskPane task = new JXTaskPane();
//        task.setName("Jetty Server");
//        task.add(getAction(StringConstants.START_JETTY.getValue()));
//        task.add(getAction(StringConstants.STOP_JETTY.getValue()));
//        task.setTitle(getMessage("main.task.servercontrol"));
//        task.setBackground(SystemColor.control);
//        taskPaneContainer.add(task, "Start server");
//        return taskPaneContainer;
//    }
    
    private MockedFieldsEditor getPropertiesEditor() {
        if(propertiesEditor != null) return propertiesEditor;
        propertiesEditor = new MockedFieldsEditor(true);
        propertiesEditor.setFields(getAllMockedFields());
        return propertiesEditor;
    }
    
    private JXTitledPanel getEditorContainer() {
    	if(this.editorContainer != null) return this.editorContainer;
    	editorContainer = new JXTitledPanel(getMessage("main.propertieseditor.title.text"));
    	editorContainer.setContentContainer(getPropertiesEditor());
    	return editorContainer;
    }
    
    private LogViewer getLogViewer() {
        if (logViewer != null)
            return logViewer;
        logViewer = new LogViewer();
        return logViewer;
    }

    public void log(String message) {
        getLogViewer().log(message);
    }
    
    public void toggleDisplayServerLog() {
        getLogViewer().toggleDisplayServerLog();
    }

}
