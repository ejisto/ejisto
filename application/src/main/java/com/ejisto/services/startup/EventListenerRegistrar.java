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

package com.ejisto.services.startup;

import com.ejisto.event.ApplicationEventDispatcher;
import com.ejisto.event.ApplicationListener;
import com.ejisto.event.def.BaseApplicationEvent;
import com.ejisto.util.GuiUtils;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: celestino
 * Date: 4/10/13
 * Time: 8:02 AM
 */
public class EventListenerRegistrar extends BaseStartupService {

    private final List<ApplicationListener<BaseApplicationEvent>> applicationListeners;
    private final ApplicationEventDispatcher eventDispatcher;

    public EventListenerRegistrar(List<ApplicationListener<BaseApplicationEvent>> applicationListeners,
                                  ApplicationEventDispatcher eventDispatcher) {
        this.applicationListeners = applicationListeners;
        this.eventDispatcher = eventDispatcher;
    }


    @Override
    public void execute() {
        applicationListeners.forEach(eventDispatcher::registerApplicationEventListener);
        GuiUtils.EVENT_DISPATCHER.set(eventDispatcher);
    }

    @Override
    public int getPriority() {
        return NORMAL_PRIORITY;
    }
}
