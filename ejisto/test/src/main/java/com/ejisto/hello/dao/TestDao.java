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

package com.ejisto.hello.dao;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.sql.*;

public class TestDao {
    private DataSource dataSource;

    public TestDao() {
        lookupDataSource();
    }

    private void lookupDataSource() {
        try {
            InitialContext ic = new InitialContext();
            this.dataSource = (DataSource) ic.lookup("jdbc/testDataSource");
        } catch (NamingException e) {
            throw new RuntimeException(e);
        }
    }

    public Date whatTimeIsIt() {
        Connection con = null;
        Date date = null;
        try {
            con = dataSource.getConnection();
            PreparedStatement st = con.prepareStatement("SELECT NOW()");
            ResultSet rs = st.executeQuery();
            if (rs.next()) date = rs.getDate(1);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        } finally {
            try {
                if (con != null) con.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return date;
    }
}
