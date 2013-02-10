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

package com.ejisto.util;

import com.ejisto.core.ApplicationException;
import com.ejisto.modules.dao.Dao;

/**
 * Created by IntelliJ IDEA.
 * User: celestino
 * Date: 2/26/11
 * Time: 6:38 PM
 */
public abstract class ExternalizableService<T extends Dao> {

    protected void checkDao() {
        T dao = getDaoInstance();
        if (dao != null) {
            return;//value injected by Spring AOP or previously created
        }
        try {
            dao = newRemoteDaoInstance();
            setDaoInstance(dao);
        } catch (Exception e) {
            throw new ApplicationException("Unable to load dao for [" + this.getClass() + "]", e);
        }
    }

    protected abstract T getDaoInstance();

    protected abstract void setDaoInstance(T daoInstance);

    protected abstract T newRemoteDaoInstance();

}
