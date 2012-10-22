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

import com.ejisto.modules.recorder.DataCollector;
import com.ejisto.modules.recorder.RequestWrapper;
import com.ejisto.modules.recorder.ResponseWrapper;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Created by IntelliJ IDEA.
 * User: celestino
 * Date: 8/24/12
 * Time: 5:18 PM
 */
public class RequestPreprocessor implements Filter {

    private ServletContext context;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        context = filterConfig.getServletContext();
        context.log("RequestPreprocessor initialized");
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        DataCollector dataCollector = new DataCollector();
        chain.doFilter(new RequestWrapper((HttpServletRequest) request, dataCollector),
                       new ResponseWrapper((HttpServletResponse) response, dataCollector));

    }

    @Override
    public void destroy() {
        context.log("destroying RequestPreprocessor");
    }

}
