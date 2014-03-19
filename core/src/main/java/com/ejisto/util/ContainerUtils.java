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

package com.ejisto.util;

import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * Created by IntelliJ IDEA.
 * User: celestino
 * Date: 3/8/11
 * Time: 9:31 PM
 */
public class ContainerUtils {

    private static final Pattern AGENT_JAR = Pattern.compile("^.*?ejisto-agent.*?\\.jar$");

    public static String extractAgentJar(String classPath) {
        String systemProperty = System.getProperty("ejisto.agent.jar.path");
        if (StringUtils.isNotBlank(systemProperty)) {
            return systemProperty;
        }

        final Optional<String> agentPath = Arrays.stream(classPath.split(Pattern.quote(File.pathSeparator)))
                .filter(e -> AGENT_JAR.matcher(e).matches())
                .findFirst();
        if(agentPath.isPresent()) {
            return Paths.get(System.getProperty("user.dir"), agentPath.get()).toString();
        }
        throw new IllegalStateException("unable to find agent jar");
    }
}
