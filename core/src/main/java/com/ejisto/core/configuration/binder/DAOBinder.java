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

package com.ejisto.core.configuration.binder;

import com.ejisto.modules.dao.CustomObjectFactoryDao;
import com.ejisto.modules.dao.MockedFieldsDao;
import com.ejisto.modules.dao.ObjectFactoryDao;
import com.ejisto.modules.dao.SettingsDao;
import com.ejisto.modules.dao.db.EmbeddedDatabaseManager;
import com.ejisto.modules.dao.local.ContainersDao;
import com.ejisto.modules.dao.local.WebApplicationDescriptorDao;
import se.jbee.inject.bind.BinderModule;

/**
 * Created by IntelliJ IDEA.
 * User: celestino
 * Date: 3/27/13
 * Time: 8:11 AM
 */
public class DAOBinder extends BinderModule {
    @Override
    protected void declare() {
        bind(MockedFieldsDao.class).to(com.ejisto.modules.dao.local.MockedFieldsDao.class);
        bind(SettingsDao.class).to(com.ejisto.modules.dao.local.SettingsDao.class);
        bind(ObjectFactoryDao.class).to(com.ejisto.modules.dao.local.ObjectFactoryDao.class);
        bind(CustomObjectFactoryDao.class).to(com.ejisto.modules.dao.local.CustomObjectFactoryDao.class);
        construct(ContainersDao.class);
        construct(WebApplicationDescriptorDao.class);
        construct(EmbeddedDatabaseManager.class);
    }
}
