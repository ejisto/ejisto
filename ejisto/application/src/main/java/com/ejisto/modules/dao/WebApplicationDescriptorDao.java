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

import com.ejisto.modules.dao.entities.WebApplicationDescriptor;
import com.ejisto.modules.dao.entities.WebApplicationDescriptorElement;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static ch.lambdaj.Lambda.forEach;

public class WebApplicationDescriptorDao  extends BaseDao {

    private static final String SQL_LOAD     = "SELECT * FROM WEBAPPLICATIONDESCRIPTOR WHERE CONTEXTPATH = ?";
    private static final String SQL_LOAD_ALL = "SELECT * FROM WEBAPPLICATIONDESCRIPTOR";
    private static final String SQL_LOAD_ELEMENTS = "SELECT * FROM WEBAPPLICATIONDESCRIPTORELEMENT WHERE CONTEXTPATH = ?";
    private static final String SQL_DELETE = "DELETE FROM WEBAPPLICATIONDESCRIPTOR WHERE CONTEXTPATH = ?";
    private static final String SQL_DELETE_ELEMENTS = "DELETE FROM WEBAPPLICATIONDESCRIPTORELEMENT WHERE CONTEXTPATH = ?";
    private static final String SQL_INSERT = "INSERT INTO WEBAPPLICATIONDESCRIPTOR (CONTEXTPATH, INSTALLATIONPATH) VALUES(?,?)";
    private static final String SQL_INSERT_ELEMENTS = "INSERT INTO WEBAPPLICATIONDESCRIPTORELEMENT (CONTEXTPATH, PATH, KIND) VALUES(?,?,?)";

    public WebApplicationDescriptor load(String contextPath) {
        WebApplicationDescriptor descriptor = getJdbcTemplate().query(SQL_LOAD, EXTRACTOR, contextPath);
        descriptor.setElements(getElements(descriptor.getContextPath()));
        return descriptor;
    }

    public List<WebApplicationDescriptor> loadAll() {
        List<WebApplicationDescriptor> descriptors = getJdbcTemplate().query(SQL_LOAD_ALL, LIST_EXTRACTOR);
        for (WebApplicationDescriptor descriptor : descriptors) {
            descriptor.setElements(getElements(descriptor.getContextPath()));
        }
        return descriptors;
    }

    public void insert(final WebApplicationDescriptor descriptor) {
        delete(descriptor);
        GeneratedKeyHolder holder = new GeneratedKeyHolder();
        getJdbcTemplate().update(new PreparedStatementCreator(){
            @Override
            public PreparedStatement createPreparedStatement(Connection con) throws SQLException {
                PreparedStatement pstm = con.prepareStatement(SQL_INSERT, new String[]{"ID"});
                pstm.setString(1, descriptor.getContextPath());
                pstm.setString(2, descriptor.getInstallationPath());
                return pstm;
            }
        }, holder);
        int id = holder.getKey().intValue();
        forEach(descriptor.getElements()).setContextPath(descriptor.getContextPath());
        insertElements(descriptor);
    }

    public void insertElements(WebApplicationDescriptor descriptor) {
        final List<WebApplicationDescriptorElement> elements = descriptor.getElements();
        getJdbcTemplate().batchUpdate(SQL_INSERT_ELEMENTS, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                WebApplicationDescriptorElement element = elements.get(i);
                ps.setString(1, element.getContextPath());
                ps.setString(2, element.getPath());
                ps.setString(3, element.getKind());
            }
            @Override
            public int getBatchSize() {
                return elements.size();
            }
        });
    }

    public void delete(WebApplicationDescriptor descriptor) {
        getJdbcTemplate().update(SQL_DELETE_ELEMENTS, descriptor.getContextPath());
        getJdbcTemplate().update(SQL_DELETE, descriptor.getContextPath());
    }


    public List<WebApplicationDescriptorElement> getElements(String contextPath) {
        return getJdbcTemplate().query(SQL_LOAD_ELEMENTS, new Object[]{contextPath}, ROW_MAPPER);
    }

    private static final RowMapper<WebApplicationDescriptorElement> ROW_MAPPER = new RowMapper<WebApplicationDescriptorElement>() {

        @Override
        public WebApplicationDescriptorElement mapRow(ResultSet rs, int rowNum) throws SQLException {
            WebApplicationDescriptorElement element = new WebApplicationDescriptorElement();
            element.setId(rs.getInt("ID"));
            element.setKind(rs.getString("KIND"));
            element.setPath(rs.getString("PATH"));
            element.setContextPath(rs.getString("CONTEXTPATH"));
            return element;
        }
    };

    private static final ResultSetExtractor<List<WebApplicationDescriptor>> LIST_EXTRACTOR = new ResultSetExtractor<List<WebApplicationDescriptor>>() {
        @Override
        public List<WebApplicationDescriptor> extractData(ResultSet rs) throws SQLException, DataAccessException {
            List<WebApplicationDescriptor> ret = new ArrayList<WebApplicationDescriptor>();
            while(rs.next()) ret.add(buildFromResultSet(rs));
            return ret;
        }
    };

    private static final ResultSetExtractor<WebApplicationDescriptor> EXTRACTOR = new ResultSetExtractor<WebApplicationDescriptor>() {
        @Override
        public WebApplicationDescriptor extractData(ResultSet rs) throws SQLException, DataAccessException {
            if(!rs.next()) return null;
            return buildFromResultSet(rs);
        }
    };

    private static WebApplicationDescriptor buildFromResultSet(ResultSet rs) throws SQLException {
        WebApplicationDescriptor descriptor = new WebApplicationDescriptor();
            descriptor.setId(rs.getInt("ID"));
            descriptor.setContextPath(rs.getString("CONTEXTPATH"));
            descriptor.setInstallationPath(rs.getString("INSTALLATIONPATH"));
            return descriptor;
    }
    
}
