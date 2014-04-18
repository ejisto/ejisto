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

package com.ejisto.services.startup;

import com.ejisto.event.ApplicationEventDispatcher;
import com.ejisto.event.def.BaseApplicationEvent;
import com.ejisto.modules.vertx.VertxManager;
import com.ejisto.util.GUIEvents;
import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.json.JsonObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: celestino
 * Date: 4/18/14
 * Time: 8:07 AM
 */
public class GUIEventHandler extends BaseStartupService {

    private static final Map<String, EventFactory<? extends BaseApplicationEvent>> REGISTRY = new HashMap<>();
    static {
        REGISTRY.put("StartContainer", GUIEvents::startServer);
        REGISTRY.put("StopContainer", GUIEvents::stopServer);
    }

    private final Handler<Message<JsonObject>> guiEventHandler;

    public GUIEventHandler(ApplicationEventDispatcher applicationEventDispatcher) {
        this.guiEventHandler = m -> {
            final JsonObject body = m.body();
            String type = m.address();
            if(REGISTRY.containsKey(type)) {
                applicationEventDispatcher.broadcast(REGISTRY.get(type).build(this, body));
            }
            m.reply(true);
        };
    }

    @Override
    public void execute() {
        REGISTRY.keySet().forEach(k -> VertxManager.registerEventHandler(k, guiEventHandler));
    }

    @Override
    public int getPriority() {
        return Integer.MAX_VALUE;
    }

    @FunctionalInterface
    interface EventFactory<T> {
        T build(Object source, JsonObject properties);
    }
}
