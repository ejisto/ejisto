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

package com.ejisto.modules.dao.remote;

import com.ejisto.modules.web.util.JSONUtil;
import com.ejisto.util.IOUtils;

import javax.servlet.http.HttpUtils;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.concurrent.Semaphore;

import static com.ejisto.constants.StringConstants.HTTP_INTERFACE_ADDRESS;
import static com.ejisto.constants.StringConstants.SESSION_RECORDING_ACTIVE;
import static java.lang.String.format;
import static java.lang.System.getProperty;

/**
 * Created by IntelliJ IDEA.
 * User: celestino
 * Date: 7/3/12
 * Time: 11:43 AM
 */
public class BaseRemoteDao {

    private static final Semaphore concurrentRequestManager = new Semaphore(50);
    private static final String SERVER_ADDRESS = "http://localhost:%s";
    private final String serverAddress;

    public BaseRemoteDao() {
        String address = getProperty(HTTP_INTERFACE_ADDRESS.getValue());
        serverAddress = address != null ? address : format(SERVER_ADDRESS, getProperty("ejisto.http.port"));
    }

    protected String remoteCall(String request, String requestPath) {
        return remoteCall(request, requestPath, null);
    }

    protected String remoteCall(String request, String requestPath, String method) {
        boolean acquired = false;
        try {
            concurrentRequestManager.acquire();
            acquired = true;
            HttpURLConnection connection = openConnection(requestPath, method);
            OutputStream out = connection.getOutputStream();
            out.write(request.getBytes());
            out.flush();
            out.close();
            return IOUtils.readInputStream(connection.getInputStream(), "UTF-8");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("thread interrupted", e);
        } catch (IOException e) {
            throw new IllegalStateException("IOException", e);
        } finally {
            if (acquired) {
                concurrentRequestManager.release();
            }
        }
    }



    protected <R> String encodeRequest(R request) {
        return JSONUtil.encode(request);
    }

    private HttpURLConnection openConnection(String requestPath, String method) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) new URL(serverAddress + requestPath).openConnection();
        connection.setDoInput(true);
        connection.setDoOutput(true);
        if(method != null) {
            connection.setRequestMethod(method.toUpperCase());
        }
        connection.connect();
        return connection;
    }

}
