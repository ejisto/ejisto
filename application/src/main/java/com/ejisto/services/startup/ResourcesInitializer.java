/*
 * Ejisto, a powerful developer assistant
 *
 * Copyright (C) 2010-2013 Celestino Bellone
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

import com.ejisto.constants.StringConstants;
import com.ejisto.core.ApplicationException;
import com.ejisto.event.EventManager;
import com.ejisto.event.def.*;
import com.ejisto.event.def.ChangeServerStatus.Command;
import com.ejisto.modules.dao.db.EmbeddedDatabaseManager;
import com.ejisto.modules.executor.TaskManager;
import com.ejisto.modules.gui.EjistoAction;
import com.ejisto.modules.gui.components.Header;
import com.ejisto.util.GuiUtils;
import lombok.extern.log4j.Log4j;
import org.jdesktop.swingx.JXHeader;
import org.jdesktop.swingx.plaf.LookAndFeelAddons;

import java.awt.*;
import java.io.File;

import static com.ejisto.constants.StringConstants.*;
import static com.ejisto.util.GuiUtils.putAction;

@Log4j
public class ResourcesInitializer extends BaseStartupService {
    private final EventManager eventManager;
    private final EmbeddedDatabaseManager dataSource;
    private final TaskManager taskManager;

    public ResourcesInitializer(EventManager eventManager, EmbeddedDatabaseManager dataSource, TaskManager taskManager) {
        this.eventManager = eventManager;
        this.dataSource = dataSource;
        this.taskManager = taskManager;
    }

    @Override
    public void execute() {
        log.info("executing ResourcesInitializer");
        File baseDir = new File(System.getProperty("user.home"), ".ejisto");
        if (!baseDir.exists()) {
            initBaseDir(baseDir);
        }
        initDirectories(baseDir);
        initDb();
        initStoredWebApps();
        initFonts();
        initDefaultActions();
    }

    @Override
    public int getPriority() {
        return 1;
    }

    private void initDirectories(File baseDir) {
        File containersDir = new File(baseDir, "containers");
        File data = new File(baseDir, "data");
        File dbScript = new File(data, "db.ej");
        File libDir = new File(baseDir, "lib");
        File libExtDir = new File(libDir, "ext");
        File deployables = new File(baseDir, "deployables");
        File runtime = new File(baseDir, "runtime");
        File temp = new File(baseDir, "temp");
        initDirectories(data, containersDir, libDir, libExtDir, new File(baseDir, "log"), deployables, runtime,
                        temp);
        System.setProperty(CONTAINERS_HOME_DIR.getValue(), containersDir.getAbsolutePath());
        System.setProperty(DB_SCRIPT.getValue(), dbScript.getAbsolutePath());
        System.setProperty(INITIALIZE_DATABASE.getValue(), String.valueOf(!dbScript.exists()));
        System.setProperty(LIB_DIR.getValue(), libDir.getAbsolutePath() + File.separator);
        System.setProperty(EXTENSIONS_DIR.getValue(), libExtDir.getAbsolutePath());
        System.setProperty(DEPLOYABLES_DIR.getValue(), deployables.getAbsolutePath());
        System.setProperty(RUNTIME_DIR.getValue(), runtime.getAbsolutePath());
        System.setProperty("java.io.tmpdir", temp.getAbsolutePath());
    }

    private void initDb() {
        try {
            dataSource.initDb(System.getProperty(DB_SCRIPT.getValue()));
        } catch (Exception e) {
            throw new ApplicationException(e);
        }
    }

    private void initDirectories(File... directories) {
        for (File directory : directories) {
            if (!directory.exists() && !directory.mkdirs()) {
                eventManager.publishEventAndWait(new ApplicationError(this, ApplicationError.Priority.FATAL, null));
            }
        }
    }

    private void initBaseDir(File baseDir) {
        if (!baseDir.mkdirs()) {
            eventManager.publishEventAndWait(new ApplicationError(this, ApplicationError.Priority.FATAL, null));
        }
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
            log.error("unexpected error during Fonts initialization", e);
        }

        String a = JXHeader.uiClassID;//initialize JXHeader.class
        log.debug("initializing JXHeader [" + a + "]");
        LookAndFeelAddons.getAddon().loadDefaults(
                new Object[]{"JXHeader.descriptionFont", defaultFont, "JXHeader.titleFont", bold, "JXTitledPanel.titleFont", bold, "JXHeader.background", Color.white});
        GuiUtils.setDefaultFont(defaultFont);
    }

    private void initDefaultActions() {
        putAction(new EjistoAction<>(new LoadWebApplication(this), true, taskManager));
        putAction(new EjistoAction<>(new ShutdownRequest(this)));
        putAction(new EjistoAction<>(
                new ChangeServerStatus(this, StringConstants.DEFAULT_CONTAINER_ID.getValue(), Command.STARTUP)));
        putAction(new EjistoAction<>(
                new ChangeServerStatus(this, StringConstants.DEFAULT_CONTAINER_ID.getValue(), Command.SHUTDOWN)));
        putAction(new EjistoAction<>(new DialogRequested(this, DialogRequested.DialogType.ABOUT)));
    }
}
