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

package com.ejisto.event.listener;

import com.ejisto.event.ApplicationListener;
import com.ejisto.event.EventManager;
import com.ejisto.event.def.ApplicationError;
import com.ejisto.event.def.ApplicationScanRequired;
import com.ejisto.event.def.BlockingTaskProgress;
import com.ejisto.modules.dao.entities.WebApplicationDescriptor;
import com.ejisto.util.IOUtils;
import lombok.extern.log4j.Log4j;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

import static org.apache.commons.collections.CollectionUtils.isEmpty;

/**
 * Created by IntelliJ IDEA.
 * User: celestino
 * Date: 8/3/12
 * Time: 9:35 PM
 */
@Log4j
public class WebApplicationScanner implements ApplicationListener<ApplicationScanRequired> {

    private final EventManager eventManager;

    public WebApplicationScanner(EventManager eventManager) {
        this.eventManager = eventManager;
    }

    @Override
    public void onApplicationEvent(ApplicationScanRequired event) {
        WebApplicationDescriptor descriptor = event.getWebApplicationDescriptor();
        Path outputPath = Paths.get(System.getProperty("java.io.tmpdir"), event.getRequestId(),
                                    descriptor.getContextPath());
        final List<String> includedJars = descriptor.getWhiteListContent();
        String id = UUID.randomUUID().toString();
        eventManager.publishEvent(new BlockingTaskProgress(this, id, "application.deploy.preprocessing.title",
                                                           "application.deploy.preprocessing.description",
                                                           "icon.work.in.progress", true));
        try {
            IOUtils.initPath(outputPath);
            IOUtils.copyFullDirContent(Paths.get(descriptor.getDeployablePath()), outputPath);
            Path classesDir = outputPath.resolve("WEB-INF").resolve("classes");
            Path libDir = outputPath.resolve("WEB-INF").resolve("lib");
            includedJars.forEach(j -> {
                try {
                    Path filePath = libDir.resolve(j);
                    IOUtils.unzipFile(filePath.toFile(), classesDir);
                    Files.delete(filePath);
                } catch (IOException e) {
                    notifyError(e);
                }
            });
            IOUtils.copyEjistoLibs(false, libDir.toFile());
        } catch (Exception ex) {
            notifyError(ex);
        }
        eventManager.publishEventAndWait(new BlockingTaskProgress(this, id, null, null, null, false));
    }



    private void notifyError(Exception ex) {
        log.error("exception during scan", ex);
        eventManager.publishEvent(new ApplicationError(this, ApplicationError.Priority.HIGH, ex));
    }

    @Override
    public Class<ApplicationScanRequired> getTargetEventType() {
        return ApplicationScanRequired.class;
    }
}
