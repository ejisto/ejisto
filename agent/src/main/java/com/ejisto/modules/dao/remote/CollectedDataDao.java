/*
 * Ejisto, a powerful developer assistant
 *
 * Copyright (C) 2010-2012  Celestino Bellone
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

package com.ejisto.modules.dao.remote;

import com.ejisto.modules.recorder.CollectedData;
import lombok.extern.java.Log;

import java.util.logging.Level;

/**
 * Created by IntelliJ IDEA.
 * User: celestino
 * Date: 10/23/12
 * Time: 6:49 PM
 */
@Log
public class CollectedDataDao extends BaseRemoteDao {

    public CollectedDataDao(String serverAddress) {
        super(serverAddress);
        log.log(Level.INFO, "dao initialized with remote address: " + serverAddress);
    }

    public void sendCollectedData(CollectedData data, String contextPath) {
        remoteCall(encodeRequest(data), contextPath + "/record", "POST");
    }

    public void registerSession(String id, String contextPath) {
        remoteCall(id, contextPath, "PUT");
    }
}
