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

package com.ejisto.modules.cargo.util;

import com.ejisto.util.PropertyChangePublisher;
import lombok.extern.log4j.Log4j;
import org.codehaus.cargo.container.installer.ZipURLInstaller;

import javax.swing.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;

import static com.ejisto.util.IOUtils.copyFile;
import static java.lang.String.format;

/**
 * Created by IntelliJ IDEA.
 * User: celestino
 * Date: 3/10/11
 * Time: 10:40 PM
 */
@Log4j
public class ContainerInstaller extends ZipURLInstaller implements PropertyChangePublisher {
    private final URL url;
    private final List<PropertyChangeListener> propertyChangeListeners;

    public ContainerInstaller(URL remoteLocation, String installDir) {
        super(remoteLocation, System.getProperty("java.io.tmpdir"), installDir);
        log.debug("super constructor called");
        this.url = remoteLocation;
        this.propertyChangeListeners = new ArrayList<PropertyChangeListener>();
    }

    @Override
    protected void download() {
        log.debug("called download()");
        if (url.getProtocol().equals("file")) copyFromLocalFile();
        else downloadFromNetwork();
    }

    private void downloadFromNetwork() {
        try {
            log.debug(format("trying to download from %s", url.toString()));
            URLConnection connection = url.openConnection();
            log.debug(format("trying to download from %s", url.toString()));
            connection.setConnectTimeout(1000);
            connection.setReadTimeout(10000);
            connection.connect();
            int total = connection.getContentLength();
            BufferedInputStream bis = new BufferedInputStream(connection.getInputStream());
            FileOutputStream out = new FileOutputStream(getDestinationFile());
            FileChannel ch = out.getChannel();
            byte[] buffer = new byte[512000];
            int read;
            int totalRead = 0;
            fireProgressChange(0);
            while ((read = bis.read(buffer)) != -1) {
                totalRead += read;
                log.trace("read " + totalRead + " of " + total);
                ch.write(ByteBuffer.wrap(buffer, 0, read));
                fireProgressChange(Math.max(50, totalRead / total * 100));
            }
            ch.close();
            out.close();
        } catch (SocketTimeoutException e) {
            log.error("caught SocketTimeoutException. About to throw DownloadTimeout", e);
            throw new DownloadTimeout("cannot open connection to " + url.toString(), e);
        } catch (IOException e) {
            log.error("caught IOException. About to throw DownloadFailed", e);
            throw new DownloadFailed("cannot download from " + url.toString(), e);
        }
    }

    private void copyFromLocalFile() {
        try {
            copyFile(new File(url.toURI()), getDestinationFile());
        } catch (Exception e) {
            throw new RuntimeException("local file URL is not valid ", e);
        }
    }

    private File getDestinationFile() {
        return new File(getDownloadDir(), getSourceFileName());
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        this.propertyChangeListeners.add(listener);
    }

    private void fireProgressChange(int progress) {
        for (PropertyChangeListener listener : propertyChangeListeners) {
            firePropertyChange(listener, new PropertyChangeEvent(this, "progress", -1, progress));
        }
    }

    private void firePropertyChange(final PropertyChangeListener listener, final PropertyChangeEvent event) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                listener.propertyChange(event);
            }
        });
    }

}
