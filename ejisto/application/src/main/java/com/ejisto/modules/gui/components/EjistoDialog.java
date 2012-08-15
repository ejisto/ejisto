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

import lombok.extern.log4j.Log4j;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;

import static com.ejisto.util.GuiUtils.*;

@Log4j
public class EjistoDialog extends JDialog {
    private static final long serialVersionUID = 1533524071894496853L;
    public static final String CLOSE_ACTION_COMMAND = "close";
    private Collection<Action> actions = new LinkedHashSet<Action>();
    private JPanel content;
    private Header header;
    private JPanel buttonsBar;
    private boolean freelyCloseable;
    private String iconKey;

    public EjistoDialog(Frame owner, String title, String iconKey) {
        this(owner, title, null, true, iconKey);
    }

    public EjistoDialog(Frame owner, String title, JPanel content, boolean freelyCloseable, String iconKey, Action... actions) {
        super(owner, title, false);
        this.content = content;
        this.freelyCloseable = freelyCloseable;
        this.iconKey = iconKey;
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
        if (actions != null && actions.length > 0) {
            setActions(Arrays.asList(actions));
        }
    }

    public void registerAction(Action action) {
        log.debug("registering action [" + action.getValue(Action.ACTION_COMMAND_KEY) + "]");
        putAction(action.getValue(Action.ACTION_COMMAND_KEY), action);
        this.actions.add(action);
    }

    public void setActions(Collection<Action> actions) {
        if (CollectionUtils.isEmpty(actions)) {
            return;
        }
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
        if (b) {
            init();
        }
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
                if (isVisible() && !isFreelyCloseable()) {
                    handleClose(e);
                } else {
                    close();
                }
            }
        });
    }

    private void handleClose(WindowEvent e) {
        Action action = getActionFor(CLOSE_ACTION_COMMAND);
        if (action == null) {
            log.warn("Dialog isn't freely disposable but no action found.");
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
        if (this.buttonsBar != null) {
            return this.buttonsBar;
        }
        buttonsBar = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        buttonsBar.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Color.gray));
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
        if (this.header != null) {
            return this.header;
        }
        header = new Header();
        header.setImageKey(iconKey);
        return header;
    }
}
