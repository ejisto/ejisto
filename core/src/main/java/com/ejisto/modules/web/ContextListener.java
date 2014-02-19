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

package com.ejisto.modules.web;

import com.ejisto.constants.StringConstants;
import com.ejisto.core.classloading.ClassTransformerImpl;
import com.ejisto.core.classloading.SpringLoadedJVMPlugin;
import com.ejisto.modules.repository.ClassPoolRepository;
import com.ejisto.modules.repository.MockedFieldsRepository;
import com.ejisto.modules.web.util.ConfigurationManager;
import com.ejisto.sl.ClassTransformer;
import com.ejisto.sl.JVMPlugin;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.LoaderClassPath;
import org.apache.log4j.*;
import org.springsource.loaded.agent.SpringLoadedPreProcessor;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.ejisto.constants.StringConstants.SESSION_RECORDING_ACTIVE;
import static com.ejisto.constants.StringConstants.TARGET_CONTEXT_PATH;

/**
 * Created by IntelliJ IDEA.
 * User: celestino
 * Date: 2/20/11
 * Time: 6:13 PM
 */
public class ContextListener implements ServletContextListener {
    private ServletContext context;
    private SpringLoadedJVMPlugin transformerPlugin;
    private final AtomicBoolean sessionRecordingActive = new AtomicBoolean(false);

    static {
        String debugPath = System.getProperty(StringConstants.CLASS_DEBUG_PATH.getValue());
        if (debugPath != null) {
            CtClass.debugDump = debugPath;
        }
    }

    public ContextListener() {
        this.context = null;
    }

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        context = sce.getServletContext();
        ConfigurationManager.initConfiguration(context);
        String targetContextPath = System.getProperty(TARGET_CONTEXT_PATH.getValue());
        sessionRecordingActive.set(Boolean.getBoolean(SESSION_RECORDING_ACTIVE.getValue()));
        initLog();
        context.log("<Ejisto> ClassTransformerImpl initialization...");
        ClassTransformer classTransformer = new ClassTransformerImpl(targetContextPath, new MockedFieldsRepository(null),
                                                                 context.getRealPath("/WEB-INF/classes/"));
        SpringLoadedPreProcessor.registerGlobalPlugin(new SpringLoadedJVMPlugin());
        SpringLoadedJVMPlugin.initClassTransformer(classTransformer);
//        transformerPlugin = new SpringLoadedJVMPlugin();
//        SpringLoadedPreProcessor.registerGlobalPlugin(transformerPlugin);
        ClassPool cp = ClassPoolRepository.getRegisteredClassPool(targetContextPath);
        cp.appendClassPath(new LoaderClassPath(Thread.currentThread().getContextClassLoader()));
        context.log("<Ejisto> ClassTransformerImpl successfully initialized!");
    }

    private void initLog() {
        Logger logger = Logger.getLogger("EjistoClassTransformer");
        if (!logger.getAllAppenders().hasMoreElements()) {
            Appender appender = new ConsoleAppender(new PatternLayout("%m%n"));
            logger.addAppender(appender);
            logger.setLevel(Level.TRACE);
            logger.info("ejisto class transformer logger initialized");
            logger.trace("this is only a test");
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        context.log("removing instrumentation agent...");
        //SpringLoadedPreProcessor.unregisterGlobalPlugin(transformerPlugin);
//        JVMPlugin.disableTransformer();
        context.log("done.");
    }
}
