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

package com.ejisto.modules.dao.db;

import java.sql.Connection;
import java.sql.SQLException;

import org.apache.log4j.Logger;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.datasource.AbstractDataSource;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabase;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;

import com.ejisto.constants.StringConstants;

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
