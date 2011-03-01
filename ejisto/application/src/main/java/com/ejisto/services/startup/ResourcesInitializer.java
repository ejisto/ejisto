/*
 * Ejisto, a powerful developer assistant
 *
 * Copyright (C) 2010-2011  Celestino Bellone
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
        initDirectories(baseDir);
        try {
            dataSource.initDb();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        initStoredWebApps();
        initFonts();
        initDefaultActions();
    }

    private void initDirectories(File baseDir) {
        File jettyDir = new File(baseDir, "jetty");
        File derbyDir = new File(baseDir, "derby");
        File derbyScript = new File(derbyDir, "ejisto.sql");
        File libDir = new File(baseDir, "lib");
        File libExtDir = new File(libDir, "ext");
        File webappsDir = new File(jettyDir, "webapps");
        File deployables = new File(baseDir, "deployables");
        initDirectories(derbyDir,
                jettyDir,
                webappsDir,
                libDir,
                libExtDir,
                new File(baseDir, "log"),
                deployables);
        System.setProperty(JETTY_HOME_DIR.getValue(), jettyDir.getAbsolutePath() + File.separator);
        System.setProperty(JETTY_WEBAPPS_DIR.getValue(), webappsDir.getAbsolutePath() + File.separator);
        System.setProperty(DERBY_SCRIPT.getValue(), derbyScript.getAbsolutePath());
        System.setProperty(INITIALIZE_DATABASE.getValue(), String.valueOf(!derbyScript.exists()));
        System.setProperty(LIB_DIR.getValue(), libDir.getAbsolutePath() + File.separator);
        System.setProperty(EXTENSIONS_DIR.getValue(), libExtDir.getAbsolutePath());
        System.setProperty(DEPLOYABLES_DIR.getValue(), deployables.getAbsolutePath());
    }

    private void initDirectories(File... directories) {
        for (File directory : directories) {
            if (!directory.exists() && !directory.mkdirs())
                eventManager.publishEventAndWait(new ApplicationError(this, ApplicationError.Priority.FATAL, null));
        }
    }

    private void initBaseDir(File baseDir) {
        if (!baseDir.mkdirs())
            eventManager.publishEventAndWait(new ApplicationError(this, ApplicationError.Priority.FATAL, null));
    }

    private void initStoredWebApps() {
        LoadWebApplication event = new LoadWebApplication(this);
        event.setLoadStored(true);
        eventManager.publishEventAndWait(event);
    }

    private void initFonts() {

        Font defaultFont = null;
        Font baseFont;
        Font bold = null;
        try {
            baseFont = Font.createFont(Font.TRUETYPE_FONT, Header.class.getResourceAsStream("/fonts/DejaVuSans.ttf"));
            bold = Font.createFont(Font.TRUETYPE_FONT, Header.class.getResourceAsStream("/fonts/DejaVuSans-Bold.ttf"));
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
