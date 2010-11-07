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

package com.ejisto.modules.dao.db;

import com.ejisto.constants.StringConstants;
import org.apache.log4j.Logger;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.datasource.AbstractDataSource;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabase;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;

import java.sql.Connection;
import java.sql.SQLException;

public class EmbeddedDatabaseManager extends AbstractDataSource {
	
	private Logger logger = Logger.getLogger(EmbeddedDatabaseManager.class);
	
	private EmbeddedDatabase dataSource;


	public void initDb() throws Exception {
		logger.info("starting Derby db");
		EmbeddedDatabaseBuilder builder = new EmbeddedDatabaseBuilder(new DefaultResourceLoader() {
			@Override
			protected Resource getResourceByPath(String path) {
				return new FileSystemResource(path);
			}
		});
		builder.setType(EmbeddedDatabaseType.DERBY).setName("ejisto").addScript("classpath:sql/ejisto-schema.sql");
		if(!Boolean.getBoolean(StringConstants.INITIALIZE_DATABASE.getValue())) builder.addScript(System.getProperty(StringConstants.DERBY_SCRIPT.getValue()));
//		else builder.addScript("classpath:sql/ejisto-data.sql");
		dataSource = builder.build();
		logger.info("done");
	}

	
	@Override
	public Connection getConnection() throws SQLException {
		return dataSource.getConnection();
	}

	@Override
	public Connection getConnection(String username, String password)
			throws SQLException {
		return dataSource.getConnection();
	}


}
