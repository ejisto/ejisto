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

package com.ejisto.modules.dao;

import com.ejisto.modules.dao.entities.CustomObjectFactory;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: celestino
 * Date: 7/3/12
 * Time: 9:36 PM
 */
public interface CustomObjectFactoryDao extends Dao {

    List<CustomObjectFactory> loadAll();

    CustomObjectFactory load(String fileName);

    boolean insert(CustomObjectFactory customObjectFactory);

    boolean update(CustomObjectFactory customObjectFactory);

    boolean exists(CustomObjectFactory customObjectFactory);

    boolean save(CustomObjectFactory customObjectFactory);
}
