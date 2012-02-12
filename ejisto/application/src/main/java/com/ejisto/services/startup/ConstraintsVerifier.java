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

package com.ejisto.services.startup;

import com.ejisto.util.ContainerUtils;
import lombok.extern.log4j.Log4j;
import org.springframework.util.StringUtils;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileLock;

/**
 * Created by IntelliJ IDEA.
 * User: celestino
 * Date: 10/30/11
 * Time: 6:42 PM
 */
@Log4j
public class ConstraintsVerifier extends BaseStartupService {

    @Override
    public void execute() {
        log.info("checking if there is another process already running");
        File lockFile = new File(System.getProperty("ejisto.home"), ".lock");
        try {
            if (!lockFile.exists() && !lockFile.createNewFile())
                throw new RuntimeException("Unable to create lock file.");
            FileLock lock = new RandomAccessFile(lockFile, "rw").getChannel().tryLock();
            if (lock == null) {
                throw new RuntimeException("There is another instance already running. Ejisto won't start.");
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        log.info("checking System properties");
        String agentPath = ContainerUtils.extractAgentJar(System.getProperty("java.class.path"));
        if (!StringUtils.hasText(agentPath)) {
            log.warn("**************************************************");
            log.warn("**       ejisto-agent not found in path.        **");
            log.warn("**          Check your configuration!           **");
            log.warn("**************************************************");
        } else {
            System.setProperty("ejisto.agent.jar.path", agentPath);
        }
    }
}
