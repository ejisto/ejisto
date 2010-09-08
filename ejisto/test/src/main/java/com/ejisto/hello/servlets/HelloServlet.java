/*******************************************************************************
 * Copyright 2010 Celestino Bellone
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package com.ejisto.hello.servlets;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.ejisto.hello.beans.HelloWorldBean;

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
		PrintWriter pw = response.getWriter();
		
		pw.write("<html><head><title>Ejisto Test servlet</title></head><body><b>Title: </b>");
		pw.write(bean.getTitle());
		pw.write("<br><b>Description: </b>");
		pw.write(bean.getDescription());
		pw.write("<br><b>Hit count: </b>");
		pw.write(String.valueOf(bean.getHits()));
		pw.write("<br><b>Time stamp: </b>");
		pw.write(String.valueOf(bean.getTimestamp()));
		pw.write("</body><html>");
		pw.flush();
		
	}

}
