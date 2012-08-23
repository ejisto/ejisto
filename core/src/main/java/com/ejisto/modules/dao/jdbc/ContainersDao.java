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

import com.ejisto.modules.dao.entities.Container;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: celestino
 * Date: 3/6/11
 * Time: 8:54 AM
 */
public class ContainersDao extends BaseJdbcDao {
    private static final String LOAD_ALL = "SELECT * FROM CONTAINER";
    private static final String LOAD = "SELECT * FROM CONTAINER WHERE ID=?";
    private static final String INSERT = "INSERT INTO CONTAINER (ID, CARGOID, HOMEDIR, DESCRIPTION) VALUES(?,?,?,?)";
    private static final String DELETE = "DELETE FROM CONTAINER WHERE ID=?";

    public List<Container> loadAll() {
        return getJdbcTemplate().query(LOAD_ALL, rowMapper);
    }

    public Container load(String id) {
        return getJdbcTemplate().queryForObject(LOAD, new Object[]{id}, rowMapper);
    }

    public boolean insert(Container container) {
        return getJdbcTemplate().update(INSERT, container.getId(), container.getCargoId(), container.getHomeDir(),
                                        container.getDescription()) == 1;
    }

    public boolean delete(Container container) {
        return getJdbcTemplate().update(DELETE, container.getId()) == 1;
    }

    private static final RowMapper<Container> rowMapper = new RowMapper<Container>() {
        @Override
        public Container mapRow(ResultSet rs, int rowNum) throws SQLException {
            Container container = new Container();
            container.setId(rs.getString("ID"));
            container.setCargoId(rs.getString("CARGOID"));
            container.setHomeDir(rs.getString("HOMEDIR"));
            container.setDescription(rs.getString("DESCRIPTION"));
            return container;
        }
    };
}
