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

package com.ejisto.modules.dao.local;

import com.ejisto.modules.dao.entities.CustomObjectFactory;
import com.ejisto.modules.dao.exception.UniqueConstraintViolated;

import java.util.ArrayList;
import java.util.List;

import static ch.lambdaj.Lambda.*;
import static org.hamcrest.CoreMatchers.equalTo;

/**
 * Created by IntelliJ IDEA.
 * User: celestino
 * Date: 1/22/11
 * Time: 9:39 AM
 */
public class CustomObjectFactoryDao extends BaseLocalDao implements com.ejisto.modules.dao.CustomObjectFactoryDao {

    @Override
    public List<CustomObjectFactory> loadAll() {
        return new ArrayList<>(getDatabase().getCustomObjectFactories());
    }

    @Override
    public CustomObjectFactory load(String fileName) {
        return selectFirst(getDatabase().getCustomObjectFactories(), having(on(CustomObjectFactory.class).getFileName(),
                                                                            equalTo(fileName)));
    }

    @Override
    public boolean insert(CustomObjectFactory customObjectFactory) {
        if (load(customObjectFactory.getFileName()) != null) {
            throw new UniqueConstraintViolated(
                    "CustomObjectFactory.fileName cannot be '" + customObjectFactory.getFileName() + "'");
        }
        getDatabase().getCustomObjectFactories().add(customObjectFactory);
        tryToCommit();
        return true;
    }

    @Override
    public boolean update(CustomObjectFactory customObjectFactory) {
        getDatabase().getCustomObjectFactories().add(customObjectFactory);
        tryToCommit();
        return true;
    }

    @Override
    public boolean exists(CustomObjectFactory customObjectFactory) {
        return load(customObjectFactory.getFileName()) != null;
    }

    @Override
    public boolean save(CustomObjectFactory customObjectFactory) {
        return update(customObjectFactory);
    }

}
