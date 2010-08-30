package com.ejisto.modules.gui.components;

import static com.ejisto.util.GuiUtils.getMessage;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.SystemColor;

import javax.swing.JLabel;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;

import org.jdesktop.swingx.JXPanel;
import org.jdesktop.swingx.JXStatusBar;
import org.jdesktop.swingx.JXTaskPane;
import org.jdesktop.swingx.JXTaskPaneContainer;

import com.ejisto.event.def.ChangeServerStatus;
import com.ejisto.event.def.ChangeServerStatus.Command;
import com.ejisto.modules.gui.EjistoAction;

public class MainPanel extends JXPanel {
    private static final long serialVersionUID = -28148619997853619L;

    private JXTaskPaneContainer taskPaneContainer;
    private JXStatusBar statusBar;
    private LogViewer logViewer;
    private JTabbedPane tabbedPane;
    private JSplitPane splitPane;

    private PropertyEditor propertyEditor;

	private Header header;

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
        add(getSplitPane(), BorderLayout.CENTER);
        add(getStatusBar(), BorderLayout.SOUTH);
    }
    
    private Header getHeader() {
    	if(header != null) return header;
    	header = new Header(getMessage("main.header.title"),getMessage("main.header.description"));
    	return header;
	}

	private JSplitPane getSplitPane() {
        if(splitPane != null) return splitPane;
        splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, getTaskPaneContainer(), getTabbedPane());
        splitPane.setOneTouchExpandable(true);
        splitPane.setBackground(SystemColor.control);
        return splitPane;
    }

    private JXTaskPaneContainer getTaskPaneContainer() {
        if (taskPaneContainer != null)
            return taskPaneContainer;
        taskPaneContainer = new JXTaskPaneContainer();
        taskPaneContainer.setMinimumSize(new Dimension(200,200));
        taskPaneContainer.setPreferredSize(new Dimension(200,200));
        taskPaneContainer.setMaximumSize(new Dimension(200, Short.MAX_VALUE));
        taskPaneContainer.setBackground(SystemColor.control);
        JXTaskPane task = new JXTaskPane();
        task.setName("Jetty Server");
        task.add(new EjistoAction<ChangeServerStatus>(new ChangeServerStatus(taskPaneContainer, Command.STARTUP)));
        task.add(new EjistoAction<ChangeServerStatus>(new ChangeServerStatus(taskPaneContainer, Command.SHUTDOWN)));
        task.setTitle(getMessage("main.task.servercontrol"));
        task.setBackground(SystemColor.control);
        taskPaneContainer.add(task, "Start server");
        return taskPaneContainer;
    }

    private JXStatusBar getStatusBar() {
        if (statusBar != null)
            return statusBar;
        statusBar = new JXStatusBar();
        statusBar.setMinimumSize(new Dimension(400,20));
        statusBar.setPreferredSize(new Dimension(400,20));
        statusBar.setMaximumSize(new Dimension(Short.MAX_VALUE, 20));
        statusBar.add(new JLabel("done"));
        return statusBar;
    }

    private JTabbedPane getTabbedPane() {
        if (tabbedPane != null)
            return tabbedPane;
        tabbedPane = new JTabbedPane(JTabbedPane.BOTTOM); // TODO customize tab layout
        tabbedPane.addTab(getMessage("main.tab.log.text"), getLogViewer());
        tabbedPane.addTab(getMessage("main.tab.property.text"), getPropertyEditor());
        return tabbedPane;
    }

    private PropertyEditor getPropertyEditor() {
        if(propertyEditor != null) return propertyEditor;
        propertyEditor = new PropertyEditor();
        return propertyEditor;
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

}
