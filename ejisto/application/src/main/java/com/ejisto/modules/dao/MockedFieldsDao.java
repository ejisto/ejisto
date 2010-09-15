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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.List;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;

import com.ejisto.modules.dao.entities.MockedField;

public class MockedFieldsDao extends BaseDao {
    private static final String LOAD_ALL = "SELECT * FROM MOCKEDFIELDS";
    private static final String LOAD_BY_CLASSNAME = "SELECT * FROM MOCKEDFIELDS WHERE CONTEXTPATH=? AND CLASSNAME = ? AND FIELDNAME = ?";
    private static final String LOAD_BY_CONTEXTPATH = "SELECT * FROM MOCKEDFIELDS WHERE CONTEXTPATH = ?";
    private static final String LOAD_BY_CONTEXTPATH_CLASSNAME = "SELECT * FROM MOCKEDFIELDS WHERE CONTEXTPATH = ? AND CLASSNAME = ?";
    private static final String COUNT_BY_CONTEXTPATH_CLASSNAME = "SELECT COUNT(*) FROM MOCKEDFIELDS WHERE CONTEXTPATH = ? AND CLASSNAME = ?";
    private static final String UPDATE = "UPDATE MOCKEDFIELDS SET CONTEXTPATH = ?, CLASSNAME = ? , FIELDNAME = ?, FIELDTYPE=?, FIELDVALUE=? WHERE ID=?";
    private static final String INSERT = "INSERT INTO MOCKEDFIELDS (CONTEXTPATH,CLASSNAME,FIELDNAME,FIELDTYPE,FIELDVALUE) VALUES(?,?,?,?,?)";
    private static final String DELETE_CONTEXT     = "DELETE FROM MOCKEDFIELDS WHERE CONTEXTPATH=?";
//    private static final String DELETE_MOCKEDFIELD = "DELETE FROM MOCKEDFIELDS WHERE ID=?";

    public List<MockedField> loadAll() {
        return getJdbcTemplate().query(LOAD_ALL, new RowMapper<MockedField>() {
            @Override
            public MockedField mapRow(ResultSet rs, int rowNum) throws SQLException {
                return loadFromResultSet(rs);
            }
        });
    }
    
    public Collection<MockedField> loadContextPathFields(String contextPath) {
        return getJdbcTemplate().query(LOAD_BY_CONTEXTPATH, new Object[] {contextPath}, new RowMapper<MockedField>() {
            @Override
            public MockedField mapRow(ResultSet rs, int rowNum) throws SQLException {
                return loadFromResultSet(rs);
            }
        });
    }
    
    public List<MockedField> loadByContextPathAndClassName(String contextPath, String className) {
        return getJdbcTemplate().query(LOAD_BY_CONTEXTPATH_CLASSNAME, new Object[] {contextPath, className}, new RowMapper<MockedField>() {
            @Override
            public MockedField mapRow(ResultSet rs, int rowNum) throws SQLException {
                return loadFromResultSet(rs);
            }
        });
    }
    
    public int countByContextPathAndClassName(String contextPath, String className) {
        return getJdbcTemplate().queryForInt(COUNT_BY_CONTEXTPATH_CLASSNAME, contextPath, className);
    }

    public MockedField getMockedField(String contextPath, String className, String fieldName) {
        return getJdbcTemplate().query(LOAD_BY_CLASSNAME, new Object[]{contextPath, className, fieldName}, new ResultSetExtractor<MockedField>() {
            @Override
            public MockedField extractData(ResultSet rs) throws SQLException, DataAccessException {
            	if(rs.next()) return loadFromResultSet(rs);
            	else throw new RuntimeException("No mockedField found.");
            }
        });
    }
    
    public boolean update(final MockedField field) {
        return getJdbcTemplate().update(new PreparedStatementCreator() {
            @Override
            public PreparedStatement createPreparedStatement(Connection con) throws SQLException {
                PreparedStatement pstm = con.prepareStatement(UPDATE);
                pstm.setString(1, field.getContextPath());
                pstm.setString(2, field.getClassName());
                pstm.setString(3, field.getFieldName());
                pstm.setString(4, field.getFieldType());
                pstm.setString(5, field.getFieldValue());
                pstm.setLong  (6, field.getId());
                return pstm;
            }
        }) == 1;
    }
    
    public long insert(final MockedField field) {
        GeneratedKeyHolder holder = new GeneratedKeyHolder();
        getJdbcTemplate().update(new PreparedStatementCreator() {
            @Override
            public PreparedStatement createPreparedStatement(Connection con) throws SQLException {
                PreparedStatement pstm = con.prepareStatement(INSERT, Statement.RETURN_GENERATED_KEYS);
                pstm.setString(1, field.getContextPath());
                pstm.setString(2, field.getClassName());
                pstm.setString(3, field.getFieldName());
                pstm.setString(4, field.getFieldType());
                pstm.setString(5, field.getFieldValue());
                return pstm;
            }
        }, holder);
        return holder.getKey().longValue();
    }
    
    public void insert(Collection<MockedField> mockedFields) {
        for (MockedField mockedField : mockedFields) {
            insert(mockedField);
        }
    }
    
    public boolean deleteContext(final String contextPath) {
    	return getJdbcTemplate().update(DELETE_CONTEXT, new PreparedStatementSetter() {
			@Override
			public void setValues(PreparedStatement ps) throws SQLException {
				ps.setString(1, contextPath);
			}
		}) > 0;
    }
    
    private MockedField loadFromResultSet(ResultSet rs) throws SQLException {
        MockedField mockedField = new MockedField();
        mockedField.setId(rs.getLong("ID"));
        mockedField.setContextPath(rs.getString("CONTEXTPATH"));
        mockedField.setClassName(rs.getString("CLASSNAME"));
        mockedField.setFieldName(rs.getString("FIELDNAME"));
        mockedField.setFieldType(rs.getString("FIELDTYPE"));
        mockedField.setFieldValue(rs.getString("FIELDVALUE"));
        return mockedField;
    }
    
    
    
    

}
