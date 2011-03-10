/*
 * Ejisto, a powerful developer assistant
 *
 * Copyright (C) 2010-2011  Celestino Bellone
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

package com.ejisto.modules.cargo.logging;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.codehaus.cargo.util.log.LogLevel;

public class ServerLogger implements org.codehaus.cargo.util.log.Logger {

    private static final Logger logger = Logger.getLogger("serverLogger");
    private static final String SPACE = " ";

    @Override
    public void setLevel(LogLevel logLevel) {
        logger.setLevel(Level.toLevel(logLevel.getLevel()));
    }

    @Override
    public LogLevel getLevel() {
        return LogLevel.toLevel(logger.getLevel().toString());
    }

    @Override
    public void info(String message, String category) {
        if (logger.isInfoEnabled()) logger.info(message);
    }

    @Override
    public void warn(String message, String category) {
        logger.warn(message);
    }

    @Override
    public void debug(String message, String category) {
        if (logger.isDebugEnabled()) logger.debug(message);
    }
}
