/*
 * Ejisto, a powerful developer assistant
 *
 * Copyright (C) 2010-2011  Celestino Bellone
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

package com.ejisto.modules.dao.db;

import com.ejisto.constants.StringConstants;
import org.apache.derby.drda.NetworkServerControl;
import org.apache.derby.jdbc.ClientDriver;
import org.apache.log4j.Logger;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.jdbc.datasource.AbstractDataSource;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;

import java.io.PrintWriter;
import java.net.InetAddress;
import java.sql.Connection;
import java.sql.SQLException;

public class EmbeddedDatabaseManager extends AbstractDataSource {

    private Logger logger = Logger.getLogger(EmbeddedDatabaseManager.class);
    private SimpleDriverDataSource driverDataSource;
    private NetworkServerControl serverControl;
    private boolean started;

    public void initDb() throws Exception {
        serverControl = new NetworkServerControl(InetAddress.getByName("localhost"), 5555, "ejisto", "ejisto");
        ResourceLoader loader = new DefaultResourceLoader() {
            @Override
            protected Resource getResourceByPath(String path) {
                return new FileSystemResource(path);
            }
        };
        ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
        populator.addScript(loader.getResource("classpath:sql/ejisto-schema.sql"));
        if (!Boolean.getBoolean(StringConstants.INITIALIZE_DATABASE.getValue()))
            populator.addScript(loader.getResource(System.getProperty(StringConstants.DERBY_SCRIPT.getValue())));
//		EmbeddedDatabaseBuilder builder = new EmbeddedDatabaseBuilder(loader);
//		builder.setType(EmbeddedDatabaseType.DERBY).setName("ejisto").addScript("classpath:sql/ejisto-schema.sql");
//		if(!Boolean.getBoolean(StringConstants.INITIALIZE_DATABASE.getValue())) builder.addScript(System.getProperty(StringConstants.DERBY_SCRIPT.getValue()));
//		dataSource = builder.build();
        serverControl.start(new PrintWriter(System.out));
        checkServerStartup();
        started = true;
        driverDataSource = new SimpleDriverDataSource(new ClientDriver(),
                                                      "jdbc:derby://localhost:5555/memory:ejisto;create=true", "ejisto",
                                                      "ejisto");
        populator.populate(getConnection());
        logger.info("done");
    }

    public boolean isStarted() {
        return started;
    }

    private void checkServerStartup() throws Exception {
        int tries = 0;
        while (tries < 5) {
            try {
                Thread.sleep(500);
                pingServer();
                return;
            } catch (Exception ex) {
                tries++;
            }
        }
        pingServer();
    }

    private void pingServer() throws Exception {
        serverControl.ping();
    }

    @Override
    public Connection getConnection() throws SQLException {
        return driverDataSource.getConnection();
    }

    @Override
    public Connection getConnection(String username, String password) throws SQLException {
        return driverDataSource.getConnection();
    }

}
