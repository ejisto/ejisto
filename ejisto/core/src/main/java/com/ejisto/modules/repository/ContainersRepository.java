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

import com.ejisto.modules.cargo.NotInstalledException;
import com.ejisto.modules.dao.ContainersDao;
import com.ejisto.modules.dao.entities.Container;
import org.springframework.dao.DataAccessException;

import javax.annotation.Resource;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: celestino
 * Date: 3/6/11
 * Time: 8:42 AM
 */
public class ContainersRepository {
    private static final ContainersRepository INSTANCE = new ContainersRepository();
    private static final String DEFAULT_ID = "__DEFAULT__";
    @Resource private ContainersDao containersDao;

    public static ContainersRepository getInstance() {
        return INSTANCE;
    }

    private ContainersRepository() {}

    public List<Container> loadContainers() {
        return containersDao.loadAll();
    }

    public Container loadDefault() throws NotInstalledException {
        return loadContainer(DEFAULT_ID);
    }

    public Container registerDefaultContainer(String cargoId, String homeDir, String description) {
        return registerContainer(DEFAULT_ID, cargoId, homeDir, description);
    }

    public Container registerContainer(String id, String cargoId, String homeDir, String description) {
        Container container = new Container();
        container.setId(id);
        container.setCargoId(cargoId);
        container.setDescription(description);
        container.setHomeDir(homeDir);
        containersDao.insert(container);
        return container;
    }

    /**
     * loads a container.
     * Currently this method has a private access
     *
     * @param id container's id
     * @return Container
     * @throws NotInstalledException if container is not installed
     */
    private Container loadContainer(String id) throws NotInstalledException {
        try {
            return containersDao.load(id);
        } catch (DataAccessException e) {
            throw new NotInstalledException(id, e);
        }
    }
}
