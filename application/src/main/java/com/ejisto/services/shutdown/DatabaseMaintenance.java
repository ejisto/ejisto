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

package com.ejisto.services.shutdown;

import com.ejisto.modules.dao.db.EmbeddedDatabaseManager;
import lombok.extern.log4j.Log4j;

@Log4j
public class DatabaseMaintenance extends BaseShutdownService {

    private final EmbeddedDatabaseManager databaseManager;

    public DatabaseMaintenance(EmbeddedDatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    @Override
    public void execute() {
        try {
            if (databaseManager.getStartupCount().orElse(0) % 20 == 0) {
                databaseManager.doMaintenance();
            }
            databaseManager.shutdown();
        } catch (InterruptedException e) {
            log.fatal("Unable to do maintenance tasks", e);
        }
    }

    @Override
    public int getPriority() {
        return 1;
    }

}
