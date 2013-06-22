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

package com.ejisto.modules.web.servlet;

import com.ejisto.modules.dao.remote.CollectedDataDao;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

import static com.ejisto.constants.StringConstants.HTTP_INTERFACE_ADDRESS;
import static java.lang.System.getProperty;

/**
 * Created by IntelliJ IDEA.
 * User: celestino
 * Date: 5/21/13
 * Time: 7:33 PM
 */
public class DefaultFilter implements Filter {

    private final CollectedDataDao collectedDataDao;

    public DefaultFilter() {
        collectedDataDao = new CollectedDataDao(getProperty(HTTP_INTERFACE_ADDRESS.getValue()));
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        collectedDataDao.getCollectedDataFor((HttpServletRequest)request);
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {

    }
}
