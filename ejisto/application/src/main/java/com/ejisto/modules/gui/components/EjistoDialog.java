package com.ejisto.modules.gui.components;


import static com.ejisto.util.GuiUtils.*;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;

import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;

import org.apache.log4j.Logger;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

public class EjistoDialog extends JDialog {
    private static final long serialVersionUID = 1533524071894496853L;
    public static final String CLOSE_ACTION_COMMAND = "close";
    private static final Logger logger = Logger.getLogger(EjistoDialog.class);
    private Collection<Action> actions = new LinkedHashSet<Action>();
    private JPanel content;
    private Header header;
    private JPanel buttonsBar;
    private boolean freelyCloseable;

    public EjistoDialog(Frame owner, String title) {
        this(owner, title, null, true);
    }

    public EjistoDialog(Frame owner, String title, JPanel content, boolean freelyCloseable, Action... actions) {
        super(owner, title, false);
        this.content = content;
        this.freelyCloseable = freelyCloseable;
        setActions(actions);
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
    }

    public void setFreelyCloseable(boolean freelyCloseable) {
        this.freelyCloseable = freelyCloseable;
    }

    public boolean isFreelyCloseable() {
        return freelyCloseable;
    }

    public void setActions(Action... actions) {
        setActions(Arrays.asList(actions));
    }

    public void registerAction(Action action) {
        if (logger.isDebugEnabled()) logger.debug("registering action [" + action.getValue(Action.ACTION_COMMAND_KEY) + "]");
        putAction(action.getValue(Action.ACTION_COMMAND_KEY), action);
        this.actions.add(action);
    }

    public void setActions(Collection<Action> actions) {
        if (CollectionUtils.isEmpty(actions)) return;
        this.actions.clear();
        for (Action action : actions) {
            registerAction(action);
        }
    }

    public void setContent(JPanel content) {
        this.content = content;
        this.content.getActionMap().setParent(getActionMap());
    }

    @Override
    public void setVisible(boolean b) {
        if (b) init();
        super.setVisible(b);
    }

    public void close() {
        setVisible(false);
        dispose();
    }

    private void init() {
        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(getHeader(), BorderLayout.NORTH);
        getContentPane().add(getContent(), BorderLayout.CENTER);
        getContentPane().add(getButtonsBar(), BorderLayout.SOUTH);
        initHooks();
    }

    private void initHooks() {
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (isVisible() && !isFreelyCloseable()) handleClose(e);
                else close();
            }
        });
    }

    private void handleClose(WindowEvent e) {
        Action action = getActionFor(CLOSE_ACTION_COMMAND);
        if (action == null) {
            logger.warn("Dialog isn't freely disposable but no action found.");
            close();
        } else {
            action.actionPerformed(new ActionEvent(this, e.getID(), CLOSE_ACTION_COMMAND));
        }
    }

    public Action getActionFor(String command) {
        return getAction(command);
    }
    
    public void setHeaderTitle(String title) {
    	getHeader().setTitle(title);
    }
    
    public void setHeaderDescription(String headerDescription) {
    	getHeader().setDescription(headerDescription);
    }

    protected JPanel getButtonsBar() {
        if (this.buttonsBar != null) return this.buttonsBar;
        buttonsBar = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        buttonsBar.setBorder(BorderFactory.createMatteBorder(1,0,0,0,Color.gray));
        JButton button;
        Dimension buttonSize = new Dimension(120, 25);
        for (Action action : actions) {
            button = new JButton(action);
            button.setSize(buttonSize);
            button.setPreferredSize(buttonSize);
            buttonsBar.add(button);
        }
        return buttonsBar;
    }

    protected JPanel getContent() {
        Assert.notNull(content);
        return content;
    }

    protected Header getHeader() {
        if (this.header != null) return this.header;
        header = new Header();
        return header;
    }
}
