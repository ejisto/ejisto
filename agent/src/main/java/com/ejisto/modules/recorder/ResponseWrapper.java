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

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import java.io.IOException;

/**
 * Created by IntelliJ IDEA.
 * User: celestino
 * Date: 8/27/12
 * Time: 6:31 PM
 */
public class ResponseWrapper extends HttpServletResponseWrapper {

    private DataCollector dataCollector;

    /**
     * Constructs a response adaptor wrapping the given response.
     *
     * @throws IllegalArgumentException if the response is null
     */
    public ResponseWrapper(HttpServletResponse source, DataCollector dataCollector) {
        super(source);
        this.dataCollector = dataCollector;
    }

    @Override
    public void addCookie(Cookie cookie) {
        super.addCookie(cookie);
    }

    @Override
    public void sendError(int sc, String msg) throws IOException {
        super.sendError(sc, msg);
    }

    @Override
    public void sendError(int sc) throws IOException {
        super.sendError(sc);
    }

    @Override
    public void sendRedirect(String location) throws IOException {
        super.sendRedirect(location);
        dataCollector.addPermanentRedirection(location);
    }

    @Override
    public void addDateHeader(String name, long date) {
        dataCollector.addResponseHeader(new ResponseHeader(name, String.valueOf(date), ResponseHeader.Type.DATE));
        super.addDateHeader(name, date);
    }

    @Override
    public void setDateHeader(String name, long date) {
        dataCollector.addResponseHeader(new ResponseHeader(name, String.valueOf(date), ResponseHeader.Type.DATE));
        super.setDateHeader(name, date);
    }

    @Override
    public void setHeader(String name, String value) {
        dataCollector.addResponseHeader(new ResponseHeader(name, value, ResponseHeader.Type.STRING));
        super.setHeader(name, value);
    }

    @Override
    public void addHeader(String name, String value) {
        dataCollector.addResponseHeader(new ResponseHeader(name, value, ResponseHeader.Type.STRING));
        super.addHeader(name, value);
    }

    @Override
    public void setIntHeader(String name, int value) {
        dataCollector.addResponseHeader(new ResponseHeader(name, String.valueOf(value), ResponseHeader.Type.INT));
        super.setIntHeader(name, value);
    }

    @Override
    public void addIntHeader(String name, int value) {
        dataCollector.addResponseHeader(new ResponseHeader(name, String.valueOf(value), ResponseHeader.Type.INT));
        super.addIntHeader(name, value);
    }
}
