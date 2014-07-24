/*
 * Ejisto, a powerful developer assistant
 *
 * Copyright (C) 2010-2014 Celestino Bellone
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

package com.ejisto.modules.vertx.handler.internal;

import com.ejisto.event.EventManager;
import com.ejisto.event.def.CollectedDataReceived;
import com.ejisto.modules.recorder.CollectedData;
import com.ejisto.modules.repository.CollectedDataRepository;
import com.ejisto.modules.vertx.handler.Boilerplate;
import com.ejisto.modules.vertx.handler.ContextHandler;
import com.ejisto.modules.web.util.JSONUtil;
import lombok.extern.log4j.Log4j;
import org.vertx.java.core.MultiMap;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.http.RouteMatcher;

import static com.ejisto.modules.dao.remote.RemoteCollectedDataDao.composeDestinationContextPath;

/**
 * Created by IntelliJ IDEA.
 * User: celestino
 * Date: 7/18/14
 * Time: 4:26 PM
 */
@Log4j
public class DataCollectorHandler implements ContextHandler {

    private static final String TARGET_CONTEXT_PATH_PARAM = "targetContextPath";
    private static final String TARGET_CONTEXT_PATH = ":"+TARGET_CONTEXT_PATH_PARAM;
    private static final String OK = "OK";
    private static final String KO = "KO";
    private final EventManager eventManager;
    private final CollectedDataRepository collectedDataRepository;

    public DataCollectorHandler(EventManager eventManager, CollectedDataRepository collectedDataRepository) {
        this.eventManager = eventManager;
        this.collectedDataRepository = collectedDataRepository;
    }

    @Override
    public void addRoutes(RouteMatcher routeMatcher) {

        final String baseURL = composeDestinationContextPath(TARGET_CONTEXT_PATH);
        routeMatcher.put(baseURL +"/init", this::initSession)
                .get(baseURL + "/load", this::loadRegisteredCollectedData)
                .post(baseURL + "/record", this::receiveData);

    }

    private void initSession(HttpServerRequest request) {
        request.bodyHandler(buffer -> {
            String body = buffer.toString();
            String contextPath = request.params().get(TARGET_CONTEXT_PATH_PARAM);
            log.debug(String.format("received start notification for %s from %s", contextPath, body));
            Boilerplate.writeOutputAsJSON(OK, request.response());
        });
    }

    private void receiveData(HttpServerRequest request) {
        request.bodyHandler(buffer -> {
            CollectedData data = JSONUtil.decode(buffer.toString(), CollectedData.class);
            eventManager.publishEvent(new CollectedDataReceived(this, data));
            Boilerplate.writeOutputAsJSON(OK, request.response());
        });
    }

    private void loadRegisteredCollectedData(HttpServerRequest request) {
        final MultiMap params = request.params();
        String contextPath = params.get(TARGET_CONTEXT_PATH_PARAM);
        request.bodyHandler(buffer -> {
            String key = buffer.toString();
            final CollectedData data = collectedDataRepository.getActiveRecordedSessions().stream()
                    .filter(cd -> cd.getSmallKey().equals(key))
                    .findFirst()
                    .orElse(CollectedData.empty(request.absoluteURI().toString(), request.path()));
            Boilerplate.writeOutputAsJSON(data, request.response());
        });
    }

}
