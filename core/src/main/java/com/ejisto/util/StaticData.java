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

package com.ejisto.util;

import com.ejisto.modules.executor.TaskManager;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

/**
 * Created by IntelliJ IDEA.
 * User: celestino
 * Date: 3/28/13
 * Time: 6:23 PM
 */
public final class StaticData {
    public static final List<TaskManager.Descriptor> TASK_DESCRIPTORS = Collections.emptyList();
    public static final Properties SETTINGS;

    static {
        SETTINGS = new Properties();
        try {
            SETTINGS.load(Object.class.getResourceAsStream("/settings.properties"));
        } catch (IOException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    private StaticData() {
    }


}
