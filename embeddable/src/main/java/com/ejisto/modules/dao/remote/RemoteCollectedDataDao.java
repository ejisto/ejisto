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

package com.ejisto.modules.dao.remote;

import com.ejisto.constants.StringConstants;
import com.ejisto.modules.recorder.CollectedData;
import lombok.extern.java.Log;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;

import static com.ejisto.modules.recorder.CollectedData.buildKey;
import static com.ejisto.modules.web.util.JSONUtil.decode;
import static java.lang.String.format;

/**
 * Created by IntelliJ IDEA.
 * User: celestino
 * Date: 10/23/12
 * Time: 6:49 PM
 */
@Log
public class RemoteCollectedDataDao extends BaseRemoteDao {

    public RemoteCollectedDataDao(String serverAddress) {
        super(serverAddress);
        log.log(Level.INFO, "dao initialized with remote address: " + serverAddress);
    }

    public void sendCollectedData(CollectedData data, String contextPath) {
        if (!data.isEmpty()) {
            remoteCall(encodeRequest(data), composeDestinationContextPath(contextPath) + "/record", "POST");
        }
    }

    public CollectedData getCollectedDataFor(HttpServletRequest request) {
        Map<String, String[]> parameters = new TreeMap<String, String[]>(request.getParameterMap());
        return decode(remoteCall(encodeRequest(buildKey(parameters, false)), composeDestinationContextPath(request.getContextPath()) + "/load",
                                 "GET"), CollectedData.class);
    }

    public void registerSession(String id, String contextPath) {
        remoteCall(id, composeDestinationContextPath(contextPath) + "/init", "PUT");
    }

    public static String composeDestinationContextPath(String applicationContextPath) {
        return format("%s/%s", StringConstants.CTX_SESSION_RECORDER, applicationContextPath);
    }
}
