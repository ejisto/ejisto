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

import com.ejisto.modules.dao.entities.MockedField;
import com.ejisto.modules.dao.local.LocalCollectedDataDao;
import com.ejisto.modules.recorder.CollectedData;

import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: celestino
 * Date: 4/30/13
 * Time: 7:14 PM
 */
public class CollectedDataRepository {

    private final LocalCollectedDataDao collectedDataDao;

    public CollectedDataRepository(LocalCollectedDataDao collectedDataDao) {
        this.collectedDataDao = collectedDataDao;
    }

    public Collection<CollectedData> getActiveRecordedSessions() {
        return getDao().loadActiveRecordedSessions();
    }

    public void saveRecordedSession(String name, CollectedData collectedData) {
        getDao().persistRecordedSession(name, collectedData);
    }

    public void enableRecordedSession(String name) {
        getDao().enableRecordedSession(name);
    }

    public void disableRecordedSession(String name) {
        getDao().disableRecordedSession(name);
    }

    public List<MockedField> loadRecordedSession(String name) {
        return new ArrayList<>(getDao().loadFromRecordedSession(name));
    }

    public Map<String, CollectedData> getAllRecordedSessions() {
        return Collections.unmodifiableMap(getDao().loadPersistedRecordedSessions());
    }

    private LocalCollectedDataDao getDao() {
        return collectedDataDao;
    }

}
