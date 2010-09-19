/*******************************************************************************
 * Copyright 2010 Celestino Bellone
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package com.ejisto.modules.dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;

import com.ejisto.constants.StringConstants;
import com.ejisto.modules.dao.entities.Setting;

public class SettingsDao extends BaseDao {

    private static final String LOAD_ALL = "SELECT * FROM SETTINGS";
    private static final String LOAD_ONE = "SELECT * FROM SETTINGS WHERE SETTINGKEY = ?";
    private static final String INSERT = "INSERT INTO SETTINGS VALUES(?,?)";
    private static final String DELETE = "DELETE FROM SETTINGS WHERE SETTINGKEY=?";

    public List<Setting> loadAll() {
        return getJdbcTemplate().query(LOAD_ALL, new RowMapper<Setting>() {
            @Override
            public Setting mapRow(ResultSet rs, int rowNum) throws SQLException {
                return loadFromResultSet(rs);
            }
        });
    }

    public Setting getSetting(String key) {
        return getJdbcTemplate().query(LOAD_ONE, new Object[]{key}, new ResultSetExtractor<Setting>() {
            @Override
            public Setting extractData(ResultSet rs) throws SQLException, DataAccessException {
                return loadFromResultSet(rs);
            }
        });
    }
    
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
