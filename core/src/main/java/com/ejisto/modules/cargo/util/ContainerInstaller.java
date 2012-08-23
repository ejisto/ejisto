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

import lombok.extern.log4j.Log4j;
import org.codehaus.cargo.container.ContainerException;
import org.codehaus.cargo.container.installer.ZipURLInstaller;

import java.beans.PropertyChangeListener;
import java.io.File;
import java.net.URL;

import static com.ejisto.util.IOUtils.copyFile;

/**
 * Created by IntelliJ IDEA.
 * User: celestino
 * Date: 3/10/11
 * Time: 10:40 PM
 */
@Log4j
public class ContainerInstaller extends ZipURLInstaller {
    private final URL url;

    public ContainerInstaller(URL remoteLocation, String installDir) {
        super(remoteLocation, System.getProperty("java.io.tmpdir"), installDir);
        log.debug("super constructor called");
        this.url = remoteLocation;
    }

    @Override
    protected void download() {
        log.debug("called download()");
        if (url.getProtocol().equals("file")) {
            copyFromLocalFile();
        } else {
            downloadFromNetwork();
        }
    }

    private void downloadFromNetwork() {
        try {
            super.download();
        } catch (ContainerException e) {
            log.error("caught ContainerException. About to throw DownloadFailed", e);
            throw new DownloadFailed("cannot download from " + url.toString(), e);
        } catch (Exception e) {
            log.error("caught Exception. About to throw DownloadTimeout", e);
            throw new DownloadTimeout("cannot open connection to " + url.toString(), e);
        }
    }

    private void copyFromLocalFile() {
        try {
            copyFile(new File(url.toURI()), getDestinationFile().getParentFile());
        } catch (Exception e) {
            throw new IllegalArgumentException("local file URL is not valid ", e);
        }
    }

    private File getDestinationFile() {
        return new File(getDownloadDir(), getSourceFileName());
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
    }

}
