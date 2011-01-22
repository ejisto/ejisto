/*
 * Ejisto, a powerful developer assistant
 *
 * Copyright (C) 2011  Celestino Bellone
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

package com.ejisto.modules.dao;


import com.ejisto.modules.dao.entities.CustomObjectFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: celestino
 * Date: 1/22/11
 * Time: 9:39 AM
 */
public class CustomObjectFactoryDao extends BaseDao {
    private static final String CHECK    = "SELECT COUNT(*) FROM CUSTOMOBJECTFACTORY WHERE FILENAME=?";
    private static final String LOAD_ALL = "SELECT * FROM CUSTOMOBJECTFACTORY";
    private static final String LOAD_ONE = "SELECT * FROM CUSTOMOBJECTFACTORY WHERE FILENAME=?";
    private static final String INSERT   = "INSERT INTO CUSTOMOBJECTFACTORY(FILENAME,CHECKSUM,PROCESSED) VALUES(?,?,?)";
    private static final String UPDATE   = "UPDATE CUSTOMOBJECTFACTORY SET CHECKSUM=?,DONE=? WHERE FILENAME=?";

    public List<CustomObjectFactory> loadAll() {
        return getJdbcTemplate().query(LOAD_ALL, ROW_MAPPER);
    }

    public CustomObjectFactory load(String fileName) {
        try {
            return getJdbcTemplate().queryForObject(LOAD_ONE, ROW_MAPPER, fileName);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    public boolean insert(CustomObjectFactory customObjectFactory) {
        return getJdbcTemplate().update(INSERT, customObjectFactory.getFileName(), customObjectFactory.getChecksum(), customObjectFactory.isProcessed() ? 1:0) == 1;
    }

    public boolean update(CustomObjectFactory customObjectFactory) {
        return getJdbcTemplate().update(UPDATE, customObjectFactory.getChecksum(), customObjectFactory.isProcessed() ? 1:0, customObjectFactory.getFileName()) == 1;
    }

    public boolean exists(CustomObjectFactory customObjectFactory) {
        return getJdbcTemplate().queryForInt(CHECK, customObjectFactory.getFileName()) > 0;
    }

    public boolean save(CustomObjectFactory customObjectFactory) {
        if(exists(customObjectFactory)) return update(customObjectFactory);
        return insert(customObjectFactory);
    }

    private static final RowMapper<CustomObjectFactory> ROW_MAPPER = new RowMapper<CustomObjectFactory>() {
            @Override
            public CustomObjectFactory mapRow(ResultSet rs, int rowNum) throws SQLException {
                CustomObjectFactory factory = new CustomObjectFactory();
                factory.setFileName(rs.getString("FILENAME"));
                factory.setChecksum(rs.getString("CHECKSUM"));
                factory.setProcessed(rs.getBoolean("PROCESSED"));
                return factory;
            }
     };

}
