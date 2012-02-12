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
import java.net.URL;
import java.net.URLConnection;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;

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
        this.url = remoteLocation;
        this.propertyChangeListeners = new ArrayList<PropertyChangeListener>();
    }

    @Override
    protected void download() {
        try {
            URLConnection connection = url.openConnection();
            connection.connect();
            int total = connection.getContentLength();
            BufferedInputStream bis = new BufferedInputStream(connection.getInputStream());
            FileOutputStream out = new FileOutputStream(new File(getDownloadDir(), getSourceFileName()));
            FileChannel ch = out.getChannel();
            byte[] buffer = new byte[512000];
            int readed;
            int totalReaded = 0;
            fireProgressChange(0);

            while ((readed = bis.read(buffer)) != -1) {
                totalReaded += readed;
                log.debug("readed " + totalReaded + " of " + total);
                ch.write(ByteBuffer.wrap(buffer, 0, readed));
                fireProgressChange(Math.max(50, totalReaded / total * 100));
            }
            ch.close();
            out.close();

        } catch (IOException e) {
            log.error("cannot download container", e);
            throw new RuntimeException("cannot download from " + url.toString(), e);
        }
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
