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

package com.ejisto.util;

import com.ejisto.modules.dao.JndiDataSourcesDao;
import com.ejisto.modules.dao.entities.JndiDataSource;

import javax.annotation.Resource;
import java.util.List;

public final class JndiDataSourcesRepository {
    private static final JndiDataSourcesRepository INSTANCE = new JndiDataSourcesRepository();
    @Resource
    private JndiDataSourcesDao jndiDataSourcesDao;

    public static JndiDataSourcesRepository getInstance() {
        return INSTANCE;
    }

    public static JndiDataSource loadDataSource(String name) {
        return getInstance().getJndiDataSourcesDao().load(name);
    }

    public static List<JndiDataSource> loadDataSources() {
        return getInstance().getJndiDataSourcesDao().loadAll();
    }

    public static void store(JndiDataSource dataSource) {
        if (!getInstance().getJndiDataSourcesDao().isAlredyRegistered(dataSource.getName())) {
            getInstance().getJndiDataSourcesDao().insert(dataSource);
        }
    }

    public static void update(JndiDataSource dataSource) {
        getInstance().getJndiDataSourcesDao().update(dataSource);
    }

    public JndiDataSourcesDao getJndiDataSourcesDao() {
        return jndiDataSourcesDao;
    }

    private JndiDataSourcesRepository() {
    }
}
