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

package com.ejisto.modules.dao.jdbc;

import com.ejisto.constants.StringConstants;
import com.ejisto.modules.dao.entities.Setting;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class SettingsDao extends BaseJdbcDao implements com.ejisto.modules.dao.SettingsDao {

    private static final String LOAD_ALL = "SELECT * FROM SETTINGS";
    private static final String LOAD_ONE = "SELECT * FROM SETTINGS WHERE SETTINGKEY = ?";
    private static final String INSERT = "INSERT INTO SETTINGS VALUES(?,?)";
    private static final String DELETE = "DELETE FROM SETTINGS WHERE SETTINGKEY=?";

    @Override
    public List<Setting> loadAll() {
        return getJdbcTemplate().query(LOAD_ALL, new RowMapper<Setting>() {
            @Override
            public Setting mapRow(ResultSet rs, int rowNum) throws SQLException {
                return loadFromResultSet(rs);
            }
        });
    }

    @Override
    public Setting getSetting(String key) {
        return getJdbcTemplate().query(LOAD_ONE, new Object[]{key}, new ResultSetExtractor<Setting>() {
            @Override
            public Setting extractData(ResultSet rs) throws SQLException, DataAccessException {
                return loadFromResultSet(rs);
            }
        });
    }

    @Override
    public boolean insertSettings(final List<Setting> settings) {
        getJdbcTemplate().batchUpdate(INSERT, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                Setting setting = settings.get(i);
                ps.setString(1, setting.getKey());
                ps.setString(2, setting.getValue());
            }

            @Override
            public int getBatchSize() {
                return settings.size();
            }
        });
        return true;
    }

    @Override
    public boolean clearSettings(final List<Setting> settings) {
        getJdbcTemplate().batchUpdate(DELETE, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                Setting setting = settings.get(i);
                ps.setString(1, setting.getKey());
            }

            @Override
            public int getBatchSize() {
                return settings.size();
            }
        });
        return true;
    }

    private Setting loadFromResultSet(ResultSet rs) throws SQLException {
        Setting setting = new Setting();
        setting.setKey(rs.getString("SETTINGKEY"));
        setting.setValue(rs.getString("SETTINGVALUE"));
        setting.setHumanReadableKey(StringConstants.fromValue(setting.getValue()));
        return setting;
    }

}
