/*
 * Ejisto, a powerful developer assistant
 *
 * Copyright (C) 2010  Celestino Bellone
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

package com.ejisto.services.startup;

import com.ejisto.event.EventManager;
import com.ejisto.event.def.ApplicationError;
import com.ejisto.event.def.ChangeServerStatus;
import com.ejisto.event.def.ChangeServerStatus.Command;
import com.ejisto.event.def.LoadWebApplication;
import com.ejisto.event.def.ShutdownRequest;
import com.ejisto.modules.conf.SettingsManager;
import com.ejisto.modules.dao.db.EmbeddedDatabaseManager;
import com.ejisto.modules.gui.EjistoAction;
import com.ejisto.modules.gui.components.Header;
import com.ejisto.util.GuiUtils;
import org.apache.log4j.Logger;
import org.jdesktop.swingx.JXHeader;
import org.jdesktop.swingx.plaf.LookAndFeelAddons;

import javax.annotation.Resource;
import java.awt.*;
import java.io.File;

import static com.ejisto.constants.StringConstants.*;
import static com.ejisto.util.GuiUtils.putAction;

public class ResourcesInitializer extends BaseStartupService {
    private static final Logger logger = Logger.getLogger(ResourcesInitializer.class);
    @Resource
    private EventManager eventManager;
    @Resource
    private EmbeddedDatabaseManager dataSource;
    @Resource
    private SettingsManager settingsManager;

    @Override
    public void execute() {
        logger.info("executing ResourcesInitializer");
        File baseDir = new File(System.getProperty("user.home"), ".ejisto");
        if (!baseDir.exists()) initBaseDir(baseDir);
        String fileSeparator = System.getProperty("file.separator");
        StringBuilder path = new StringBuilder(baseDir.getAbsolutePath()).append(fileSeparator).append("jetty").append(fileSeparator);
        System.setProperty(JETTY_HOME_DIR.getValue(), path.toString());
        System.setProperty(JETTY_WEBAPPS_DIR.getValue(), path.append(fileSeparator).append("webapps").append(fileSeparator).toString());
        File webappsDir = new File(path.toString());
        if (!webappsDir.exists()) initBaseDir(webappsDir);
        path.delete(0, path.length());
        File derbyHome = new File(path.append(baseDir.getAbsolutePath()).append(fileSeparator).append("derby").append(fileSeparator).toString());
        if (!derbyHome.exists()) initBaseDir(derbyHome);
        System.setProperty(DERBY_SCRIPT.getValue(), path.append("ejisto.sql").toString());
        System.setProperty(INITIALIZE_DATABASE.getValue(), String.valueOf(!new File(path.toString()).exists()));
        try {
            dataSource.initDb();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        path.delete(0, path.length());
        File descriptorDir = new File(path.append(baseDir.getAbsolutePath()).append(fileSeparator).append("descriptors").toString());
        if (!descriptorDir.exists()) initBaseDir(descriptorDir);
        System.setProperty(DESCRIPTOR_DIR.getValue(), path.toString());
        initStoredWebApps();
        initFonts();
        initDefaultActions();
    }



    private void initBaseDir(File baseDir) {
        if (!baseDir.mkdirs()) eventManager.publishEventAndWait(new ApplicationError(this, ApplicationError.Priority.FATAL, null));
    }
    
    private void initStoredWebApps() {
        LoadWebApplication event = new LoadWebApplication(this);
        event.setLoadStored(true);
        eventManager.publishEventAndWait(event);
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
        GuiUtils.setDefaultFont(defaultFont);
    }
    
    private void initDefaultActions() {
    	putAction(new EjistoAction<LoadWebApplication>(new LoadWebApplication(this)));
    	putAction(new EjistoAction<ShutdownRequest>(new ShutdownRequest(this)));
    	putAction(new EjistoAction<ChangeServerStatus>(new ChangeServerStatus(this, Command.STARTUP)));
    	putAction(new EjistoAction<ChangeServerStatus>(new ChangeServerStatus(this, Command.SHUTDOWN)));
    }
}
