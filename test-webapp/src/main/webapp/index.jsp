    <%@ page import="com.ejisto.hello.beans.SimplePropertyValue" %>
        <%--
          ~ Ejisto, a powerful developer assistant
          ~
          ~ Copyright (C) 2010-2012  Celestino Bellone
          ~
          ~ Ejisto is free software: you can redistribute it and/or modify
          ~ it under the terms of the GNU General Public License as published by
          ~ the Free Software Foundation, either version 3 of the License, or
          ~ (at your option) any later version.
          ~
          ~ Ejisto is distributed in the hope that it will be useful,
          ~ but WITHOUT ANY WARRANTY; without even the implied warranty of
          ~ MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
          ~ GNU General Public License for more details.
          ~
          ~ You should have received a copy of the GNU General Public License
          ~ along with this program.  If not, see <http://www.gnu.org/licenses/>.
          --%>

        <%--
          Created by IntelliJ IDEA.
          User: celestino
          Date: 1/15/11
          Time: 8:24 PM
        --%>
        <%@ page contentType="text/html;charset=UTF-8" language="java" %>
        <jsp:useBean id="bean" class="com.ejisto.hello.beans.HelloWorldBean" scope="request"/>
        <html>
        <head><title>Ejisto Test servlet</title></head>
        <body>
        <b><%= bean %>
        </b><br>
        <b>Title: </b><%= bean.getTitle() %>
        <br><b>Description: </b><%= bean.getDescription() %>
        <br><b>Hit count: </b><%= bean.getHits() %>
        <br><b>Time stamp: </b><%= bean.getTimestamp() %>
        <br><b>Property values:</b><br>
            <%
    for (SimplePropertyValue propertyValue : bean.getPropertyValues()) {
%>
            <%= propertyValue.getValue() %>
        <br>
            <%
    }
%>
            <% if (bean.isDisplayHeader()) { %>
        <h2>this is the header!</h2>
            <% } else {%>
        <p>displayHeader off</p>
            <% }%>
        </body>
        </html>