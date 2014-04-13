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

package com.ejisto.event.listener;

import com.ejisto.event.ApplicationListener;
import com.ejisto.event.def.BaseApplicationEvent;
import com.ejisto.modules.vertx.VertxManager;
import com.ejisto.modules.web.util.JSONUtil;
import lombok.extern.log4j.Log4j;

/**
 * Created by IntelliJ IDEA.
 * User: celestino
 * Date: 4/11/14
 * Time: 8:01 AM
 */
@Log4j
public class VertxEventHandler implements ApplicationListener<BaseApplicationEvent> {

    @Override
    public void onApplicationEvent(BaseApplicationEvent event) {
        String message = JSONUtil.encode(event.toEventDescription());
        log.trace(String.format("publishing event to the client: %s - %s", event.getClientKey(), message));
        VertxManager.publishEvent(event.getClientKey(), message);
    }

    @Override
    public Class<BaseApplicationEvent> getTargetEventType() {
        return BaseApplicationEvent.class;
    }

}
