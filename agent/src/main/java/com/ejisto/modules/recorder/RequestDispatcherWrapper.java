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

package com.ejisto.modules.recorder;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Enumeration;

/**
 * Created by IntelliJ IDEA.
 * User: celestino
 * Date: 8/27/12
 * Time: 6:38 PM
 */
public class RequestDispatcherWrapper implements RequestDispatcher {

    private final RequestDispatcher source;
    private final String path;
    private final DataCollector dataCollector;

    public RequestDispatcherWrapper(RequestDispatcher source, String path, DataCollector dataCollector) {
        this.source = source;
        this.path = path;
        this.dataCollector = dataCollector;
    }

    @Override
    public void forward(ServletRequest request, ServletResponse response) throws ServletException, IOException {
        //checkpoint
        dumpRequestParameters(request);
        dataCollector.addResourcePath(path);
        source.forward(new RequestWrapper((HttpServletRequest) request, dataCollector),
                       new ResponseWrapper((HttpServletResponse) response, dataCollector));
    }

    @Override
    public void include(ServletRequest request, ServletResponse response) throws ServletException, IOException {
        source.include(request, response);
    }

    private void dumpRequestParameters(ServletRequest request) {
        Enumeration<?> e = request.getParameterNames();
        while (e.hasMoreElements()) {
            String name = (String) e.nextElement();
            dataCollector.putRequestParameter(name, request.getParameter(name));
        }
    }
}
