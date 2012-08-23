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

package com.ejisto.modules.dao.jdbc;

import com.ejisto.modules.dao.entities.RegisteredObjectFactory;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: celestino
 * Date: 7/31/11
 * Time: 6:46 PM
 */
public class ObjectFactoryDao extends BaseJdbcDao implements com.ejisto.modules.dao.ObjectFactoryDao {
    private static final String LOAD_ALL = "SELECT * FROM OBJECTFACTORY";
    private static final String INSERT = "INSERT INTO OBJECTFACTORY VALUES(?,?)";

    @Override
    public List<RegisteredObjectFactory> loadAll() {
        return getJdbcTemplate().query(LOAD_ALL, new RowMapper<RegisteredObjectFactory>() {
            @Override
            public RegisteredObjectFactory mapRow(ResultSet rs, int rowNum) throws SQLException {
                return new RegisteredObjectFactory(rs.getString("CLASSNAME"), rs.getString("TARGETCLASSNAME"));
            }
        });
    }

    @Override
    public void insert(RegisteredObjectFactory registeredObjectFactory) {
        getJdbcTemplate().update(INSERT, registeredObjectFactory.getClassName(),
                                 registeredObjectFactory.getTargetClassName());
    }
}
