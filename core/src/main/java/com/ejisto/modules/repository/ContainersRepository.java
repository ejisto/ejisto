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

package com.ejisto.modules.repository;

import com.ejisto.modules.cargo.NotInstalledException;
import com.ejisto.modules.dao.entities.Container;
import com.ejisto.modules.dao.local.ContainersDao;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static com.ejisto.constants.StringConstants.DEFAULT_CONTAINER_ID;

/**
 * Created by IntelliJ IDEA.
 * User: celestino
 * Date: 3/6/11
 * Time: 8:42 AM
 */
public final class ContainersRepository {

    private final ContainersDao containersDao;
    private final ConcurrentMap<String, Container> temporaryContainers = new ConcurrentHashMap<>();

    private ContainersRepository(ContainersDao containersDao) {
        this.containersDao = containersDao;
    }

    public List<Container> loadContainers() {
        return containersDao.loadAll();
    }

    public Container loadDefault() throws NotInstalledException {
        return loadContainer(DEFAULT_CONTAINER_ID.getValue());
    }

    public Container registerDefaultContainer(String cargoId, String homeDir, String description) {
        return registerContainer(DEFAULT_CONTAINER_ID.getValue(), cargoId, homeDir, description);
    }

    public void registerTemporaryContainer(Container container) {
        Container existing = temporaryContainers.putIfAbsent(container.getId(), container);
        if (existing != null) {
            throw new IllegalStateException(existing + " is not null");
        }
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
     *
     * @param id container's id
     * @return Container
     * @throws NotInstalledException if container is not installed
     */
    public Container loadContainer(String id) throws NotInstalledException {
        Objects.requireNonNull(id, "container id can't be null");
        Container result = temporaryContainers.get(id);
        if (result != null) {
            return result;
        }
        result = containersDao.load(id);
        if (result == null) {
            throw new NotInstalledException(id);
        }
        return result;
    }
}
