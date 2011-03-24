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

package com.ejisto.util;

import com.ejisto.modules.dao.BaseDao;
import org.apache.derby.jdbc.ClientDriver;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;

import javax.sql.DataSource;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by IntelliJ IDEA.
 * User: celestino
 * Date: 2/26/11
 * Time: 6:38 PM
 */
public abstract class ExternalizableService<T extends BaseDao> {

    private static DataSource remoteDataSource;
    private static ReentrantLock writeLock = new ReentrantLock();

    protected void checkDao() {
        T dao = getDaoInstance();
        if (dao != null) return;//value injected by Spring AOP or previously created
        try {
            dao = getDaoClass().newInstance();
            dao.setDataSource(getRemoteDataSource());
            setDaoInstance(dao);
        } catch (Exception e) {
            throw new RuntimeException("Unable to load dao [" + this.getDaoClass() + "]", e);
        }
    }

    protected abstract T getDaoInstance();

    protected abstract void setDaoInstance(T daoInstance);

    protected abstract Class<T> getDaoClass();

    private static DataSource getRemoteDataSource() {
        boolean locked = false;
        try {
            if (remoteDataSource != null) return remoteDataSource;
            locked = writeLock.tryLock(500, TimeUnit.MILLISECONDS);
            if (!locked) throw new RuntimeException("Unable to lock");
            remoteDataSource = new SimpleDriverDataSource(new ClientDriver(), "jdbc:derby://localhost:5555/memory:ejisto", "ejisto", "ejisto");
            return remoteDataSource;
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            if (locked) writeLock.unlock();
        }
    }
}
