/*
 * Copyright 2010 Celestino Bellone
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions
 * and limitations under the License.
 */

package com.ejisto.services.startup;

import static com.ejisto.constants.StringConstants.DERBY_SCRIPT;
import static com.ejisto.constants.StringConstants.INITIALIZE_DATABASE;
import static com.ejisto.constants.StringConstants.JETTY_HOME_DIR;
import static com.ejisto.constants.StringConstants.JETTY_WEBAPPS_DIR;

import java.awt.Color;
import java.awt.Font;
import java.io.File;

import javax.annotation.Resource;

import org.jdesktop.swingx.JXHeader;
import org.jdesktop.swingx.plaf.LookAndFeelAddons;

import com.ejisto.event.EventManager;
import com.ejisto.event.def.ApplicationError;
import com.ejisto.modules.dao.db.EmbeddedDatabaseManager;
import com.ejisto.modules.gui.components.Header;

public class ResourcesInitializer extends BaseStartupService {

    @Resource
    private EventManager eventManager;
    @Resource
    private EmbeddedDatabaseManager dataSource;

    @Override
    public void execute() {
        File baseDir = new File(System.getProperty("user.home"), ".ejisto");
        if (!baseDir.exists()) initBaseDir(baseDir);
        String fileSeparator = System.getProperty("file.separator");
        StringBuilder jettyHome = new StringBuilder(baseDir.getAbsolutePath()).append(fileSeparator).append("jetty").append(fileSeparator);
        System.setProperty(JETTY_HOME_DIR.getValue(), jettyHome.toString());
        System.setProperty(JETTY_WEBAPPS_DIR.getValue(), jettyHome.append(fileSeparator).append("webapps").append(fileSeparator).toString());
        File webappsDir = new File(jettyHome.toString());
        if (!webappsDir.exists()) initBaseDir(webappsDir);
        jettyHome.delete(0, jettyHome.length());
        File derbyHome = new File(jettyHome.append(baseDir.getAbsolutePath()).append(fileSeparator).append("derby").append(fileSeparator).toString());
        if (!derbyHome.exists()) initBaseDir(derbyHome);
        System.setProperty(DERBY_SCRIPT.getValue(), jettyHome.append("ejisto.sql").toString());
        System.setProperty(INITIALIZE_DATABASE.getValue(), String.valueOf(!new File(jettyHome.toString()).exists()));
        try {
            dataSource.initDb();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        initFonts();
    }

    private void initBaseDir(File baseDir) {
        if (!baseDir.mkdirs()) eventManager.publishEvent(new ApplicationError(this, ApplicationError.Priority.FATAL, null));
    }

    private void initFonts() {

        Font defaultFont = null;
        Font baseFont = null;
        Font bold = null;
        try {
            baseFont = Font.createFont(Font.TRUETYPE_FONT, Header.class.getResourceAsStream("/fonts/DejaVuSans.ttf"));
            bold     = Font.createFont(Font.TRUETYPE_FONT, Header.class.getResourceAsStream("/fonts/DejaVuSans-Bold.ttf"));
            defaultFont = baseFont.deriveFont(10f);
            bold = bold.deriveFont(Font.BOLD, 13f);
            
        } catch (Exception e) {
            e.printStackTrace();
        }        
        
//        String[] properties = { "Label.font", "List.font", "Panel.font", "ProgressBar.font", "Viewport.font", "TabbedPane.font", "Table.font",
//                "TableHeader.font", "TextField.font", "PasswordField.font", "TextArea.font", "TextPane.font", "EditorPane.font", "TitledBorder.font",
//                "ToolBar.font", "ToolTip.font", "Tree.font" };
//        
//        for (String property : properties) {
//            UIManager.put(property, new FontUIResource(systemFont));
//        }
        
        @SuppressWarnings("unused")
        String a = JXHeader.uiClassID;//initialize JXHeader.class
        LookAndFeelAddons.getAddon().loadDefaults(new Object[]{"JXHeader.descriptionFont", defaultFont, "JXHeader.titleFont", bold, "JXTitledPanel.titleFont", bold, "JXHeader.background", Color.white});

    }
}