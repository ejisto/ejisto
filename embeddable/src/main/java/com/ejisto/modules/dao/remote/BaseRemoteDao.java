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

package com.ejisto.modules.dao.remote;

import com.ejisto.modules.web.util.JSONUtil;
import lombok.extern.java.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.concurrent.Semaphore;
import java.util.logging.Level;

import static com.ejisto.constants.StringConstants.HTTP_INTERFACE_ADDRESS;
import static java.lang.String.format;
import static java.lang.System.getProperty;
import static org.apache.commons.lang3.StringUtils.defaultIfEmpty;
import static org.apache.commons.lang3.StringUtils.isBlank;

/**
 * Created by IntelliJ IDEA.
 * User: celestino
 * Date: 7/3/12
 * Time: 11:43 AM
 */
@Log
public class BaseRemoteDao {

    private static final Semaphore CONCURRENT_REQUEST_MANAGER = new Semaphore(50);
    private static final String SERVER_ADDRESS = "http://localhost:%s";
    private final String serverAddress;

    public BaseRemoteDao() {
        String address = getProperty(HTTP_INTERFACE_ADDRESS.getValue());
        log.log(Level.FINEST, "address is: " + address);
        serverAddress = evaluateServerAddress(address);
        log.log(Level.FINEST, "server address set to: " + this.serverAddress);
    }

    protected BaseRemoteDao(String serverAddress) {
        this.serverAddress = evaluateServerAddress(serverAddress);
        log.log(Level.FINEST, "server address set to: " + this.serverAddress);
    }

    private static String evaluateServerAddress(String in) {
        if (isBlank(in)) {
            return format(SERVER_ADDRESS, getProperty("ejisto.http.port"));
        }
        return in;
    }

    protected String remoteCall(String request, String requestPath) {
        return remoteCall(request, requestPath, null);
    }

    protected String remoteCall(String request, String requestPath, String method) {
        boolean acquired = false;
        try {
            CONCURRENT_REQUEST_MANAGER.acquire();
            acquired = true;
            HttpURLConnection connection = openConnection(requestPath, method);
            OutputStream out = connection.getOutputStream();
            out.write(request.getBytes());
            out.flush();
            out.close();
            return new String(readInputStream(connection.getInputStream()), Charset.forName("UTF-8"));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("thread interrupted", e);
        } catch (IOException e) {
            throw new IllegalStateException("IOException", e);
        } finally {
            if (acquired) {
                CONCURRENT_REQUEST_MANAGER.release();
            }
        }
    }

    private byte[] readInputStream(InputStream in) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream(in.available());
        int read;
        byte[] buffer = new byte[4096];
        while ((read = in.read(buffer)) != -1) {
            out.write(buffer, 0, read);
        }
        return out.toByteArray();
    }

    protected <R> String encodeRequest(R request) {
        return JSONUtil.encode(request);
    }

    private HttpURLConnection openConnection(String requestPath, String method) throws IOException {
        String destination = serverAddress + defaultIfEmpty(requestPath, "/");
        log.log(Level.FINEST, "url destination: " + destination);
        HttpURLConnection connection = (HttpURLConnection) new URL(destination).openConnection();
        connection.setDoInput(true);
        connection.setDoOutput(true);
        if (method != null) {
            connection.setRequestMethod(method.toUpperCase());
        }
        connection.connect();
        return connection;
    }

}
