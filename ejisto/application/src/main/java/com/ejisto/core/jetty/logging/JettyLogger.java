/*
 * Copyright 2010 Celestino Bellone
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions
 * and limitations under the License.
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
