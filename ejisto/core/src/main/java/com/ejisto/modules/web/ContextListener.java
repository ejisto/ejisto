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

package com.ejisto.modules.web;

import com.ejisto.InstrumentationHolder;
import com.ejisto.constants.StringConstants;
import com.ejisto.core.classloading.ClassTransformer;
import org.apache.derby.jdbc.ClientDriver;
import org.apache.log4j.*;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Created by IntelliJ IDEA.
 * User: celestino
 * Date: 2/20/11
 * Time: 6:13 PM
 */
public class ContextListener implements ServletContextListener {
    private Driver driver;
    private ServletContext context;

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        Logger logger = Logger.getLogger("EjistoClassTransformer");
        if (!logger.getAllAppenders().hasMoreElements()) {
            Appender appender = new ConsoleAppender(new TTCCLayout());
            logger.addAppender(appender);
            logger.setLevel(Level.TRACE);
        }
        context = sce.getServletContext();
        context.log("Ejisto initializing...");
        String targetContextPath = context.getInitParameter(StringConstants.CONTEXT_PARAM_NAME.getValue());
        InstrumentationHolder.getInstrumentation().addTransformer(new ClassTransformer(targetContextPath));
        driver = new ClientDriver();
        initDataSource();
        context.log("Ejisto successfully initialized!");
    }

    private void initDataSource() {
        DataSourceHolder.setDataSource(new SimpleDriverDataSource(driver, "jdbc:derby://localhost:5555/memory:ejisto", "ejisto", "ejisto"));
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        try {
            DriverManager.deregisterDriver(driver);
        } catch (SQLException e) {
            context.log("error during driver deregistration", e);
        }
        //TODO notify event
    }
}
