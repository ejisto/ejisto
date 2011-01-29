/*
 * Ejisto, a powerful developer assistant
 *
 * Copyright (C) 2011  Celestino Bellone
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

package com.ejisto.hello.servlets;

import com.ejisto.hello.beans.HelloWorldBean;
import com.ejisto.hello.dao.TestDao;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

public class HelloServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    /**
     * @see HttpServlet#HttpServlet()
     */
    public HelloServlet() {
        super();
    }

    /**
     * @see HttpServlet#service(HttpServletRequest request, HttpServletResponse response)
     */
    protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html");
        HelloWorldBean bean = new HelloWorldBean();
        TestDao dao = new TestDao();
        request.setAttribute("bean", bean);
        request.setAttribute("dao", dao);
//        RequestDispatcher dispatcher = getServletContext().getRequestDispatcher("index.jsp");
//        dispatcher.forward(request, response);
        PrintWriter pw = response.getWriter();
        pw.write("<html><head><title>Ejisto Test servlet</title></head><body><b>Title: </b>");
        pw.write(bean.getTitle());
        pw.write("<br><b>Description: </b>");
        pw.write(bean.getDescription());
        pw.write("<br><b>Hit count: </b>");
        pw.write(String.valueOf(bean.getHits()));
        pw.write("<br><b>Time stamp: </b>");
        pw.write(String.valueOf(bean.getTimestamp()));
        pw.write("<br><b>what time is it?:</b>");
        pw.write(String.valueOf(dao.whatTimeIsIt()));
        pw.write("</body><html>");
        pw.flush();

    }


}
