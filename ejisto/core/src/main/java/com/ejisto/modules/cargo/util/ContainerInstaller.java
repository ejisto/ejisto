/*
 * Ejisto, a powerful developer assistant
 *
 * Copyright (C) 2010-2011  Celestino Bellone
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

import org.apache.log4j.Logger;
import org.codehaus.cargo.container.installer.ZipURLInstaller;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

/**
 * Created by IntelliJ IDEA.
 * User: celestino
 * Date: 3/10/11
 * Time: 10:40 PM
 */
public class ContainerInstaller extends ZipURLInstaller {
    private static final Logger logger = Logger.getLogger(ContainerInstaller.class);
    private URL url;

    public ContainerInstaller(URL remoteLocation, String installDir) {
        super(remoteLocation, installDir);
        this.url = remoteLocation;
    }

    @Override
    protected void download() {
        try {
            URLConnection connection = url.openConnection();
            connection.connect();
            int total = connection.getContentLength();
            BufferedInputStream bis = new BufferedInputStream(connection.getInputStream());
            FileOutputStream out = new FileOutputStream(new File(getDestinationDir(), getSourceFileName()));
            FileChannel ch = out.getChannel();
            byte[] buffer = new byte[512000];
            int readed;
            int totalReaded = 0;
            while ((readed = bis.read(buffer)) != -1) {
                totalReaded += readed;
                if (logger.isDebugEnabled()) logger.debug("readed " + totalReaded + " of " + total);
                ch.write(ByteBuffer.wrap(buffer, 0, readed));
            }
            ch.close();
            out.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
