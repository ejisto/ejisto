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

package com.ejisto.modules.gui.components.helper;

import ch.lambdaj.function.closure.Closure0;
import ch.lambdaj.function.closure.Closure1;
import com.ejisto.modules.dao.entities.JndiDataSource;
import com.ejisto.modules.gui.components.JndiResourcesEditor;
import com.ejisto.modules.validation.NumberValidator;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import org.jdesktop.swingx.JXLabel;
import org.jdesktop.swingx.JXTitledSeparator;
import org.springframework.util.CollectionUtils;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.Map;

import static ch.lambdaj.Lambda.var;
import static com.ejisto.constants.StringConstants.SELECT_FILE_COMMAND;
import static com.ejisto.util.GuiUtils.getMessage;

public class BoundResourceEditor {
    private JPanel boundResourceEditor;
    private JTextField driverJarPath;
    private JTextField driverClassName;
    private JTextField url;
    private JTextField username;
    private JTextField password;
    private JFormattedTextField maxActive;
    private JFormattedTextField maxIdle;
    private JFormattedTextField maxWait;
    private JLabel resourceName;
    private JLabel resourceType;
    private JLabel nameLabel;
    private JLabel typeLabel;
    private JLabel jarPathLabel;
    private JLabel classNameLabel;
    private JLabel urlLabel;
    private JLabel usernameLabel;
    private JLabel passwordLabel;
    private JLabel maxActiveLabel;
    private JLabel maxIdleLabel;
    private JLabel maxWaitLabel;
    private JXTitledSeparator connectionTitle;
    private JXTitledSeparator poolTitle;
    private Map<String, Closure1<String>> actionMap;
    private JndiDataSource dataSource;
    private JndiResourcesEditor container;
    private int index;
    private Closure0 reloadElement;

    public BoundResourceEditor(JndiDataSource dataSource, JndiResourcesEditor container, int index) {
        this.dataSource = dataSource;
        this.actionMap = new HashMap<String, Closure1<String>>();
        this.container = container;
        this.index = index;
        initClosures();
        $$$setupUI$$$();
        setData(dataSource);
    }

    public JPanel getBoundResourceEditor() {
        return boundResourceEditor;
    }

    public ActionMap getActionMap() {
        return getBoundResourceEditor().getActionMap();
    }

    public void setJarFilePath(String path) {
        driverJarPath.setText(path);
    }

    private void createUIComponents() {
        driverJarPath = createTextField("driverJarPath");
        driverJarPath.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        getBoundResourceEditor().getActionMap().get(SELECT_FILE_COMMAND.getValue()).actionPerformed(
                                new ActionEvent(BoundResourceEditor.this, 0, SELECT_FILE_COMMAND.getValue()));
                    }
                });
            }
        });
        driverClassName = createTextField("driverClassName");
        url = createTextField("url");
        username = createTextField("username");
        password = createTextField("password");
        maxActive = createIntegerTextField("maxActive");
        maxIdle = createIntegerTextField("maxIdle");
        maxWait = createIntegerTextField("maxWait");
        nameLabel = new JXLabel(getMessage("wizard.resource.name"));
        typeLabel = new JXLabel(getMessage("wizard.resource.type"));
        jarPathLabel = new JXLabel(getMessage("wizard.resource.driver.jar.file"));
        classNameLabel = new JXLabel(getMessage("wizard.resource.driverClassName"));
        urlLabel = new JXLabel(getMessage("wizard.resource.url"));
        usernameLabel = new JXLabel(getMessage("wizard.resource.username"));
        passwordLabel = new JXLabel(getMessage("wizard.resource.password"));
        maxActiveLabel = new JXLabel(getMessage("wizard.resource.maxActive"));
        maxIdleLabel = new JXLabel(getMessage("wizard.resource.maxIdle"));
        maxWaitLabel = new JXLabel(getMessage("wizard.resource.maxWait"));
        connectionTitle = new JXTitledSeparator(getMessage("wizard.resource.connection"));
        poolTitle = new JXTitledSeparator(getMessage("wizard.resource.pool"));
    }

    private void setData(JndiDataSource data) {
        driverJarPath.setText(data.getDriverJarPath());
        driverClassName.setText(data.getDriverClassName());
        url.setText(data.getUrl());
        username.setText(data.getUsername());
        password.setText(data.getPassword());
        maxActive.setValue(data.getMaxActive());
        maxIdle.setValue(data.getMaxIdle());
        maxWait.setValue(data.getMaxWait());
        resourceName.setText(data.getName());
        getBoundResourceEditor().setName(data.getName());
        resourceType.setText(data.getType());
    }

    private JTextField createTextField(String propertyName) {
        JTextField field = new JTextField();
        field.getDocument().addDocumentListener(
                new ClosurePropertyChangeListener(propertyName, actionMap, reloadElement));
        return field;
    }

    private JFormattedTextField createIntegerTextField(String propertyName) {
        JFormattedTextField field = new JFormattedTextField();
        field.addPropertyChangeListener("value",
                                        new ClosurePropertyChangeListener(propertyName, actionMap, reloadElement));
        field.setInputVerifier(new NumberValidator());
        field.addFocusListener(new TextComponentFocusListener());
        return field;
    }

    void setMaxActive(String value) {
        dataSource.setMaxActive(Integer.parseInt(value));
    }

    void setMaxWait(String value) {
        dataSource.setMaxWait(Integer.parseInt(value));
    }

    void setMaxIdle(String value) {
        dataSource.setMaxIdle(Integer.parseInt(value));
    }

    void reloadElement() {
        container.reloadElement(index);
    }

    private void initClosures() {
        if (!CollectionUtils.isEmpty(actionMap)) {
            return;
        }
        actionMap.put("driverClassName", new Closure1<String>() {{
            of(dataSource).setDriverClassName(var(String.class));
        }});
        actionMap.put("driverJarPath", new Closure1<String>() {{
            of(dataSource).setDriverJarPath(var(String.class));
        }});
        actionMap.put("url", new Closure1<String>() {{
            of(dataSource).setUrl(var(String.class));
        }});
        actionMap.put("username", new Closure1<String>() {{
            of(dataSource).setUsername(var(String.class));
        }});
        actionMap.put("password", new Closure1<String>() {{
            of(dataSource).setPassword(var(String.class));
        }});
        actionMap.put("maxActive", new Closure1<String>() {{
            of(BoundResourceEditor.this).setMaxActive(var(String.class));
        }});
        actionMap.put("maxWait", new Closure1<String>() {{
            of(BoundResourceEditor.this).setMaxWait(var(String.class));
        }});
        actionMap.put("maxIdle", new Closure1<String>() {{
            of(BoundResourceEditor.this).setMaxIdle(var(String.class));
        }});

        reloadElement = new Closure0() {{
            of(BoundResourceEditor.this).reloadElement();
        }};

    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        createUIComponents();
        boundResourceEditor = new JPanel();
        boundResourceEditor.setLayout(new FormLayout(
                "fill:max(d;4px):noGrow,left:100px:noGrow,left:3dlu:noGrow,left:70px:noGrow,left:3dlu:noGrow,fill:60px:noGrow,left:72px:noGrow",
                "center:d:noGrow,top:4dlu:noGrow,center:max(d;4px):noGrow,top:4dlu:noGrow,center:max(d;4px):noGrow,top:4dlu:noGrow,center:max(d;4px):noGrow,top:4dlu:noGrow,center:max(d;4px):noGrow,top:4dlu:noGrow,center:max(d;4px):noGrow,top:4dlu:noGrow,center:max(d;4px):noGrow,top:4dlu:noGrow,center:max(d;4px):noGrow,top:4dlu:noGrow,center:max(d;4px):noGrow,top:4dlu:noGrow,center:max(d;4px):noGrow"));
        CellConstraints cc = new CellConstraints();
        boundResourceEditor.add(nameLabel, cc.xy(2, 1));
        resourceName = new JLabel();
        resourceName.setText("Label");
        boundResourceEditor.add(resourceName, cc.xyw(4, 1, 4));
        boundResourceEditor.add(typeLabel, cc.xy(2, 3));
        resourceType = new JLabel();
        resourceType.setText("type");
        boundResourceEditor.add(resourceType, cc.xyw(4, 3, 4));
        boundResourceEditor.add(connectionTitle, cc.xyw(2, 5, 6));
        boundResourceEditor.add(jarPathLabel, cc.xy(2, 7));
        boundResourceEditor.add(driverJarPath, cc.xyw(4, 7, 4, CellConstraints.FILL, CellConstraints.DEFAULT));
        boundResourceEditor.add(classNameLabel, cc.xy(2, 9));
        boundResourceEditor.add(driverClassName, cc.xyw(4, 9, 4, CellConstraints.FILL, CellConstraints.DEFAULT));
        boundResourceEditor.add(urlLabel, cc.xy(2, 11));
        boundResourceEditor.add(url, cc.xyw(4, 11, 4, CellConstraints.FILL, CellConstraints.DEFAULT));
        boundResourceEditor.add(usernameLabel, cc.xy(2, 13));
        boundResourceEditor.add(username, cc.xy(4, 13, CellConstraints.FILL, CellConstraints.DEFAULT));
        boundResourceEditor.add(passwordLabel, cc.xy(6, 13));
        boundResourceEditor.add(password, cc.xy(7, 13, CellConstraints.FILL, CellConstraints.DEFAULT));
        boundResourceEditor.add(poolTitle, cc.xyw(2, 15, 6));
        boundResourceEditor.add(maxActiveLabel, cc.xy(2, 17));
        boundResourceEditor.add(maxActive, cc.xy(4, 17, CellConstraints.FILL, CellConstraints.DEFAULT));
        boundResourceEditor.add(maxIdleLabel, cc.xy(6, 17));
        boundResourceEditor.add(maxIdle, cc.xy(7, 17, CellConstraints.FILL, CellConstraints.DEFAULT));
        boundResourceEditor.add(maxWaitLabel, cc.xy(2, 19));
        boundResourceEditor.add(maxWait, cc.xy(4, 19, CellConstraints.FILL, CellConstraints.DEFAULT));
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() { return boundResourceEditor; }
}
