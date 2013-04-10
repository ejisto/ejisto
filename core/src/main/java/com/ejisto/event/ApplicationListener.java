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

package com.ejisto.event;

import com.ejisto.event.def.BaseApplicationEvent;

import java.util.EventListener;

/**
 * Created by IntelliJ IDEA.
 * User: celestino
 * Date: 4/4/13
 * Time: 7:59 AM
 */
public interface ApplicationListener<T extends BaseApplicationEvent> extends EventListener {

    void onApplicationEvent(T event);

    Class<T> getTargetEvent();

}
