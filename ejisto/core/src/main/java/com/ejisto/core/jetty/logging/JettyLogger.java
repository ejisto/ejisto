/*
 * Ejisto, a powerful developer assistant
 *
 * Copyright (C) 2010  Celestino Bellone
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

package com.ejisto.core.jetty.logging;

import org.apache.log4j.Logger;


public class JettyLogger implements org.eclipse.jetty.util.log.Logger  {

    private static final Logger logger = Logger.getLogger("jettylogger");
    private static final String SPACE = " ";
    
    @Override
    public boolean isDebugEnabled() {
        return logger.isDebugEnabled();
    }

    @Override
    public void setDebugEnabled(boolean enabled) {
        //nothing to be done.
    }

    @Override
    public void debug(String s, Object... objects) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void debug(Throwable throwable) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void debug(String msg, Throwable th) {
        logger.debug(msg, th);
    }

    @Override
    public void warn(String msg, Throwable th) {
        logger.warn(msg,th);
    }

    @Override
    public void info(String s, Object... objects) {
        logger.info(concatMessage(s,objects));
    }

    @Override
    public void info(Throwable throwable) {
        logger.info(throwable);
    }

    @Override
    public void info(String s, Throwable throwable) {
        logger.info(s,throwable);
    }

    @Override
    public org.eclipse.jetty.util.log.Logger getLogger(String name) {
        return this;
    }

    @Override
    public String getName() {
        return "ejisto";
    }

    @Override
    public void warn(String s, Object... objects) {
        logger.warn(concatMessage(s,objects));
    }

    @Override
    public void warn(Throwable throwable) {
        logger.warn(throwable);
    }

    private String concatMessage(String msg, Object... args) {
        StringBuilder message = new StringBuilder(msg);
        for (Object arg : args) {
            message.append(SPACE).append(arg);
        }
        return message.toString();
    }


}
