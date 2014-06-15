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

import com.ejisto.core.ApplicationException;
import com.ejisto.event.EventManager;
import com.ejisto.event.def.ApplicationError;
import com.ejisto.event.def.LoadWebApplication;
import com.ejisto.modules.dao.db.EmbeddedDatabaseManager;
import lombok.extern.log4j.Log4j;

import java.io.File;
import java.util.Arrays;

import static com.ejisto.constants.StringConstants.*;

@Log4j
public class ResourcesInitializer extends BaseStartupService {
    private final EventManager eventManager;
    private final EmbeddedDatabaseManager dataSource;

    public ResourcesInitializer(EventManager eventManager, EmbeddedDatabaseManager dataSource) {
        this.eventManager = eventManager;
        this.dataSource = dataSource;
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
    }

    @Override
    public int getPriority() {
        return 3;
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

    private void initDirectories(File... directories) {
        Arrays.stream(directories).forEach(directory -> {
            if (!directory.exists() && !directory.mkdirs()) {
                eventManager.publishEventAndWait(new ApplicationError(this, ApplicationError.Priority.FATAL, null));
            }
        });
    }

    private void initDb() {
        try {
            dataSource.initDb(System.getProperty(DB_SCRIPT.getValue()));
        } catch (Exception e) {
            throw new ApplicationException(e);
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
}
