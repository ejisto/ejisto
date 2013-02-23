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

package com.ejisto.modules.dao.local;

import com.ejisto.modules.dao.entities.JndiDataSource;

import java.util.ArrayList;
import java.util.List;

import static ch.lambdaj.Lambda.*;
import static org.hamcrest.CoreMatchers.equalTo;

public class JndiDataSourcesDao extends BaseLocalDao {

    public JndiDataSource insert(final JndiDataSource dataSource) {
        dataSource.setId(getDatabase().getNextJndiDataSourceSequenceValue());
        getDatabase().getJndiDataSources().add(dataSource);
        tryToCommit();
        return dataSource;
    }

    public void update(final JndiDataSource dataSource) {
        getDatabase().getJndiDataSources().add(dataSource);
        tryToCommit();
    }

    public List<JndiDataSource> loadAll() {
        return new ArrayList<>(getDatabase().getJndiDataSources());
    }

    public JndiDataSource load(String name) {
        return selectFirst(getDatabase().getJndiDataSources(),
                           having(on(JndiDataSource.class).getName(), equalTo(name)));
    }

    public boolean isAlreadyRegistered(String name) {
        return load(name) != null;
    }

}
