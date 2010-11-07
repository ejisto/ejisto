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

package com.ejisto.modules.dao;

import com.ejisto.modules.dao.entities.JndiDataSource;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;

import java.sql.*;
import java.util.List;

public class JndiDataSourcesDao extends BaseDao {
    private static final String LOAD_ALL = "SELECT * FROM JNDI_DATASOURCE";
    private static final String LOAD_ONE = "SELECT * FROM JNDI_DATASOURCE WHERE RESOURCENAME=?";
    private static final String INSERT = "INSERT INTO JNDI_DATASOURCE (RESOURCENAME,RESOURCETYPE,DRIVERCLASSNAME,CONNECTIONURL,DRIVERJAR,USERNAME,PASSWORD,MAXACTIVE,MAXWAIT,MAXIDLE) VALUES(?,?,?,?,?,?,?,?,?,?)";
    private static final String UPDATE = "UPDATE JNDI_DATASOURCE SET RESOURCENAME=?,RESOURCETYPE=?,DRIVERCLASSNAME=?,CONNECTIONURL=?,DRIVERJAR=?,USERNAME=?,PASSWORD=?,MAXACTIVE=?,MAXWAIT=?,MAXIDLE=?";

    public JndiDataSource insert(final JndiDataSource dataSource) {
        GeneratedKeyHolder holder = new GeneratedKeyHolder();
        getJdbcTemplate().update(new PreparedStatementCreator() {
            @Override
            public PreparedStatement createPreparedStatement(Connection con) throws SQLException {
                PreparedStatement pstm = con.prepareStatement(INSERT, Statement.RETURN_GENERATED_KEYS);
                fillStatement(pstm, dataSource);
                return pstm;
            }
        }, holder);
        dataSource.setId(holder.getKey().longValue());
        return dataSource;
    }

    public void update(final JndiDataSource dataSource) {
        getJdbcTemplate().update(new PreparedStatementCreator() {
            @Override
            public PreparedStatement createPreparedStatement(Connection con) throws SQLException {
                PreparedStatement pstm = con.prepareStatement(UPDATE);
                fillStatement(pstm, dataSource);
                return pstm;
            }
        });
    }

    public List<JndiDataSource> loadAll() {
        return getJdbcTemplate().query(LOAD_ALL, new RowMapper<JndiDataSource>() {
            @Override
            public JndiDataSource mapRow(ResultSet rs, int rowNum) throws SQLException {
                return buildDataSource(rs);
            }
        });
    }

    public JndiDataSource load(String name) {
        return getJdbcTemplate().query(LOAD_ONE, new Object[]{name}, new ResultSetExtractor<JndiDataSource>() {
            @Override
            public JndiDataSource extractData(ResultSet rs) throws SQLException, DataAccessException {
                if (!rs.next()) return null;
                return buildDataSource(rs);
            }
        });
    }

    public boolean isAlredyRegistered(String name) {
        return load(name) != null;
    }

    private JndiDataSource buildDataSource(ResultSet rs) throws SQLException {
        JndiDataSource dataSource = new JndiDataSource();
        dataSource.setId(rs.getLong("ID"));
        dataSource.setName(rs.getString("RESOURCENAME"));
        dataSource.setType(rs.getString("RESOURCETYPE"));
        dataSource.setDriverClassName(rs.getString("DRIVERCLASSNAME"));
        dataSource.setUrl(rs.getString("CONNECTIONURL"));
        dataSource.setDriverJarPath(rs.getString("DRIVERJAR"));
        dataSource.setUsername(rs.getString("USERNAME"));
        dataSource.setPassword(rs.getString("PASSWORD"));
        dataSource.setMaxActive(rs.getInt("MAXACTIVE"));
        dataSource.setMaxWait(rs.getLong("MAXWAIT"));
        dataSource.setMaxIdle(rs.getInt("MAXIDLE"));
        return dataSource;
    }


    private void fillStatement(PreparedStatement st, JndiDataSource dataSource) throws SQLException {
        st.setString(1, dataSource.getName());
        st.setString(2, dataSource.getType());
        st.setString(3, dataSource.getDriverClassName());
        st.setString(4, dataSource.getUrl());
        st.setString(5, dataSource.getDriverJarPath());
        st.setString(6, dataSource.getUsername());
        st.setString(7, dataSource.getPassword());
        st.setInt(8, dataSource.getMaxActive());
        st.setLong(9, dataSource.getMaxWait());
        st.setInt(10, dataSource.getMaxIdle());
    }


}
