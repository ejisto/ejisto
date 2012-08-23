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

package com.ejisto.modules.dao.db;

import com.ejisto.constants.StringConstants;
import lombok.extern.log4j.Log4j;
import org.apache.derby.drda.NetworkServerControl;
import org.apache.derby.jdbc.ClientDriver;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.jdbc.datasource.AbstractDataSource;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import java.util.zip.GZIPInputStream;

import static com.ejisto.constants.StringConstants.*;
import static com.ejisto.util.IOUtils.findFirstAvailablePort;
import static java.lang.String.format;

@Log4j
public class EmbeddedDatabaseManager extends AbstractDataSource implements InitializingBean {

    private SimpleDriverDataSource driverDataSource;
    private NetworkServerControl serverControl;
    @javax.annotation.Resource(name = "settings") private Properties settings;
    private DatabaseConfiguration databaseConfiguration;

    public void initDb() throws Exception {
        System.setProperty(DATABASE_PORT.getValue(), String.valueOf(databaseConfiguration.port));
        serverControl = new NetworkServerControl(InetAddress.getByName("localhost"), databaseConfiguration.port,
                                                 databaseConfiguration.user, databaseConfiguration.password);
        ResourceLoader loader = new DefaultResourceLoader() {
            @Override
            protected Resource getResourceByPath(String path) {
                return new GZipResource(path);
            }
        };
        ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
        populator.addScript(loader.getResource("classpath:sql/ejisto-schema.sql"));
        if (!Boolean.getBoolean(StringConstants.INITIALIZE_DATABASE.getValue())) {
            populator.addScript(loader.getResource(System.getProperty(StringConstants.DB_SCRIPT.getValue())));
        }
        serverControl.start(new PrintWriter(System.out));
        checkServerStartup();
        initDatabase();
        driverDataSource = new SimpleDriverDataSource(new ClientDriver(),
                                                      databaseConfiguration.url,
                                                      databaseConfiguration.user,
                                                      databaseConfiguration.password);
        populator.populate(getConnection());
        log.info("done");
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

    private void initDatabase() throws SQLException {
        DriverManager.registerDriver(new ClientDriver());
        Connection con = null;
        try {
            con = DriverManager.getConnection(databaseConfiguration.url + ";create=true",
                                              databaseConfiguration.user,
                                              databaseConfiguration.password);
        } finally {
            if (con != null) {
                con.close();
            }
        }
    }

    @Override
    public Connection getConnection() throws SQLException {
        return driverDataSource.getConnection();
    }

    @Override
    public Connection getConnection(String username, String password) throws SQLException {
        return driverDataSource.getConnection();
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        int port = findFirstAvailablePort(5555);
        this.databaseConfiguration = new DatabaseConfiguration(format("jdbc:derby://localhost:%s/memory:ejisto", port),
                                                               settings.getProperty(DATABASE_USER.getValue()),
                                                               settings.getProperty(DATABASE_PWD.getValue()), port);
        if (Boolean.getBoolean("force.db.init")) {
            //since we're in a Test environment, we need to force db initialization
            initDb();
        }
    }

    private static final class GZipResource extends FileSystemResource {
        public GZipResource(String path) {
            super(path);
        }

        @Override
        public InputStream getInputStream() throws IOException {
            return new GZIPInputStream(super.getInputStream());
        }
    }

    private static final class DatabaseConfiguration {
        private final String url;
        private final String user;
        private final String password;
        private final int port;

        public DatabaseConfiguration(String url, String user, String password, int port) {
            this.url = url;
            this.user = user;
            this.password = password;
            this.port = port;
        }
    }

}
