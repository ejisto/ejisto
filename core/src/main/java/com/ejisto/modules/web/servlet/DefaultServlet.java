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

import com.ejisto.modules.dao.remote.RemoteCollectedDataDao;
import com.ejisto.modules.recorder.CollectedData;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

import static com.ejisto.constants.StringConstants.HTTP_INTERFACE_ADDRESS;
import static java.lang.System.getProperty;

/**
 * Created by IntelliJ IDEA.
 * User: celestino
 * Date: 5/20/13
 * Time: 6:32 PM
 */
public class DefaultServlet extends HttpServlet {

    private ServletContext context;
    private final RemoteCollectedDataDao collectedDataDao;

    public DefaultServlet() {
        super();
        this.collectedDataDao = new RemoteCollectedDataDao(getProperty(HTTP_INTERFACE_ADDRESS.getValue()));
    }

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        this.context = config.getServletContext();
    }

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        CollectedData collectedData = collectedDataDao.getCollectedDataFor(request);
        if (!collectedData.getRequestDispatcherRedirection().isEmpty()) {
            request.getRequestDispatcher(collectedData.getRequestDispatcherRedirection().get(0)).forward(request,
                                                                                                         response);
        }
        if (!collectedData.getPermanentRedirection().isEmpty()) {
            response.sendRedirect(collectedData.getPermanentRedirection().get(0));
        }
        context.log("unable to redirect");
        response.setContentType("text/html");
        final PrintWriter writer = response.getWriter();
        writer.append("<html><head><title>Cannot process request</title></head>");
        writer.append("<body><strong>Cannot process request</strong><br>");
        writer.append("<p>ejisto cannot process your request. This error should be due to a lack of configuration.<p>");
        writer.append("<p>Here two useful tips:<p>");
        writer.append("<ul><li>start a recording session in order to collect some (more) data</li>");
        writer.append("<li>enable the server \"hybrid\" mode: go to Settings/Deploy/hybrid mode</li></ul>");
        writer.append(
                "<p>If you think that this error could be a bug, please create a new issue in the issue tracker. Thank you</p>");
        writer.append("</body></html>");
        writer.flush();
    }
}
