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

import ch.lambdaj.Lambda;
import com.ejisto.modules.dao.db.EmbeddedDatabaseManager;
import com.ejisto.modules.dao.entities.MockedField;
import com.ejisto.modules.recorder.CollectedData;
import org.apache.commons.lang3.StringUtils;
import org.hamcrest.CoreMatchers;
import org.hamcrest.beans.HasPropertyWithValue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import static ch.lambdaj.Lambda.extractProperty;
import static ch.lambdaj.Lambda.select;
import static org.hamcrest.CoreMatchers.equalTo;

/**
 * Created by IntelliJ IDEA.
 * User: celestino
 * Date: 4/30/13
 * Time: 8:33 AM
 */
public class CollectedDataDao extends BaseLocalDao {

    public CollectedDataDao(EmbeddedDatabaseManager database) {
        super(database);
    }

    public void persistRecordedSession(final String name, final CollectedData collectedData) {
        transactionalOperation(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                getDatabase().createNewRecordingSession(name, collectedData);
                return null;
            }
        });
    }

    public Map<String, CollectedData> loadPersistedRecordedSessions() {
        return getDatabase().getRecordedSessions();
    }

    public Collection<CollectedData> loadActiveRecordedSessions() {
        return getDatabase().getActiveRecordedSessions();
    }

    public Collection<MockedField> loadFromRecordedSession(String sessionName) {
        return new ArrayList<>(getDatabase().getRecordedSession(sessionName).getAllFields());
    }

    public Collection<MockedField> loadFromRecordedSessionByRequestURI(String requestURI) {
        List<CollectedData> selected = select(getDatabase().getActiveRecordedSessions(),
                                                       new HasPropertyWithValue<>("requestURI", equalTo(requestURI)));
        return extractProperty(selected, "allFields");
    }

    public void enableRecordedSession(final String name) {
        changeActivationState(name, true);
    }

    public void disableRecordedSession(String name) {
        changeActivationState(name, false);
    }

    private void changeActivationState(final String name, final boolean active) {
        transactionalOperation(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                final EmbeddedDatabaseManager database = getDatabase();
                CollectedData current = database.getRecordedSession(name);
                database.createNewRecordingSession(name, CollectedData.changeActivationState(current, active));
                return null;
            }
        });
    }

}
