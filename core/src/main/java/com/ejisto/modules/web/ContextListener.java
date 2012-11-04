/*
 * Ejisto, a powerful developer assistant
 *
 * Copyright (C) 2010-2012  Celestino Bellone
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

package com.ejisto.modules.web;

import com.ejisto.InstrumentationHolder;
import com.ejisto.constants.StringConstants;
import com.ejisto.core.classloading.ClassTransformer;
import com.ejisto.modules.repository.ClassPoolRepository;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.LoaderClassPath;
import org.apache.log4j.*;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.ejisto.constants.StringConstants.HTTP_INTERFACE_ADDRESS;
import static com.ejisto.constants.StringConstants.SESSION_RECORDING_ACTIVE;

/**
 * Created by IntelliJ IDEA.
 * User: celestino
 * Date: 2/20/11
 * Time: 6:13 PM
 */
public class ContextListener implements ServletContextListener {
    private ServletContext context;
    private ClassTransformer classTransformer;
    private final AtomicBoolean sessionRecordingActive = new AtomicBoolean(false);

    static {
        String debugPath = System.getProperty(StringConstants.CLASS_DEBUG_PATH.getValue());
        if (debugPath != null) {
            CtClass.debugDump = debugPath;
        }
    }

    public ContextListener() {
        this.context = null;
        this.classTransformer = null;
    }

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        context = sce.getServletContext();
        String targetContextPath = context.getInitParameter(StringConstants.TARGET_CONTEXT_PATH.getValue());
        System.setProperty(HTTP_INTERFACE_ADDRESS.getValue(),
                           context.getInitParameter(HTTP_INTERFACE_ADDRESS.getValue()));
        sessionRecordingActive.set(Boolean.parseBoolean(context.getInitParameter(SESSION_RECORDING_ACTIVE.getValue())));
        if (sessionRecordingActive.get()) {
            context.log("<Ejisto> Session recording is active, thus ClassTransformer won't be initialized.");
            return;
        }
        initLog();
        context.log("<Ejisto> ClassTransformer initialization...");
        classTransformer = new ClassTransformer(targetContextPath);
        InstrumentationHolder.getInstrumentation().addTransformer(classTransformer);
        ClassPool cp = ClassPoolRepository.getRegisteredClassPool(targetContextPath);
        cp.appendClassPath(new LoaderClassPath(Thread.currentThread().getContextClassLoader()));
        context.log("<Ejisto> ClassTransformer successfully initialized!");
    }

    private void initLog() {
        Logger logger = Logger.getLogger("EjistoClassTransformer");
        if (!logger.getAllAppenders().hasMoreElements()) {
            Appender appender = new ConsoleAppender(new TTCCLayout());
            logger.addAppender(appender);
            logger.setLevel(Level.TRACE);
            logger.info("ejisto class transformer logger initialized");
            logger.trace("this is only a test");
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        if (!sessionRecordingActive.get()) {
            context.log("removing instrumentation agent...");
            InstrumentationHolder.getInstrumentation().removeTransformer(classTransformer);
            context.log("done.");
        }
    }
}
