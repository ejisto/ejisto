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

import com.ejisto.modules.dao.remote.CollectedDataDao;
import com.ejisto.modules.recorder.DataCollector;
import com.ejisto.modules.recorder.RequestWrapper;
import com.ejisto.modules.recorder.ResponseWrapper;
import com.ejisto.modules.web.util.ConfigurationManager;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.ejisto.constants.StringConstants.HTTP_INTERFACE_ADDRESS;
import static com.ejisto.modules.web.util.DigestUtil.sha256Digest;
import static java.lang.System.getProperty;

/**
 * Created by IntelliJ IDEA.
 * User: celestino
 * Date: 8/24/12
 * Time: 5:18 PM
 */
public class RequestPreprocessor implements Filter {

    private static final ExecutorService EXECUTOR_SERVICE = Executors.newFixedThreadPool(10);
    private ServletContext context;
    private String contextPath;
    private CollectedDataDao collectedDataDao;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        context = filterConfig.getServletContext();
        contextPath = context.getContextPath();
        try {
            ConfigurationManager.initConfiguration(context);
            this.collectedDataDao = new CollectedDataDao(getProperty(HTTP_INTERFACE_ADDRESS.getValue()));
            context.log("trying to register the application:");
            collectedDataDao.registerSession(generateApplicationId(), context.getContextPath());
            context.log("done.");
        } catch (UnknownHostException e) {
            throw new ServletException(e);
        }
        context.log("RequestPreprocessor initialized");
    }

    @Override
    public void doFilter(final ServletRequest request, final ServletResponse response, FilterChain chain) throws IOException, ServletException {
        final DataCollector dataCollector = new DataCollector((HttpServletRequest) request);
        chain.doFilter(new RequestWrapper((HttpServletRequest) request, dataCollector),
                       new ResponseWrapper((HttpServletResponse) response, dataCollector));
        EXECUTOR_SERVICE.submit(new Runnable() {
            @Override
            public void run() {
                context.log("send collected results...");
                try {
                    collectedDataDao.sendCollectedData(dataCollector.getResult(),
                                                       contextPath);
                } catch (Exception ex) {
                    context.log("send failed", ex);
                }
                context.log("done.");
            }
        });
    }

    @Override
    public void destroy() {
        context.log("destroying RequestPreprocessor");
    }

    private String generateApplicationId() throws UnknownHostException {
        StringBuilder id = new StringBuilder();
        InetAddress localhost = InetAddress.getLocalHost();
        id.append(localhost.getCanonicalHostName()).append("-");
        id.append(localhost.getHostAddress());
        id.append(context.getServerInfo()).append("-");
        id.append(context.getContextPath());
        return sha256Digest(id.toString());

    }

}
