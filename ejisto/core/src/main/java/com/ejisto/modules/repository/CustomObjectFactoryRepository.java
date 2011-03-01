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

package com.ejisto.modules.repository;

import com.ejisto.modules.dao.CustomObjectFactoryDao;
import com.ejisto.modules.dao.entities.CustomObjectFactory;
import com.ejisto.util.ExternalizableService;

import javax.annotation.Resource;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: celestino
 * Date: 2/21/11
 * Time: 10:23 PM
 */
public class CustomObjectFactoryRepository extends ExternalizableService<CustomObjectFactoryDao> {
    private static final CustomObjectFactoryRepository INSTANCE = new CustomObjectFactoryRepository();
    @Resource
    private CustomObjectFactoryDao customObjectFactoryDao;

    public static CustomObjectFactoryRepository getInstance() {
        return INSTANCE;
    }

    public List<CustomObjectFactory> getCustomObjectFactories() {
        return customObjectFactoryDao.loadAll();
    }

    @Override
    protected CustomObjectFactoryDao getDaoInstance() {
        return customObjectFactoryDao;
    }

    @Override
    protected void setDaoInstance(CustomObjectFactoryDao daoInstance) {
        this.customObjectFactoryDao = daoInstance;
    }

    @Override
    protected Class<CustomObjectFactoryDao> getDaoClass() {
        return CustomObjectFactoryDao.class;
    }
}
