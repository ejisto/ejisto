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

package com.ejisto.util;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

/**
 * Created by IntelliJ IDEA.
 * User: celestino
 * Date: 2/24/12
 * Time: 6:32 PM
 */
public class ContainerUtilsTest {

    @Test
    public void testExtractAgentJar() throws Exception {
        String path = "lib/:lib/ant-1.7.1.jar:lib/ant-launcher-1.7.1.jar:lib/aopalliance-1.0.jar:lib/asm-3.1.jar:lib/cargo-core-api-container-1.1.3.jar:lib/cargo-core-api-generic-1.1.3.jar:lib/cargo-core-api-module-1.1.3.jar:lib/cargo-core-api-util-1.1.3.jar:lib/cargo-core-container-tomcat-1.1.3.jar:lib/cglib-2.2.jar:lib/commons-codec-1.4.jar:lib/commons-collections-3.2.1.jar:lib/commons-dbcp-1.4.jar:lib/commons-discovery-0.4.jar:lib/commons-logging-1.1.1.jar:lib/commons-pool-1.5.4.jar:lib/derby-10.6.1.0.jar:lib/derbyclient-10.6.1.0.jar:lib/derbynet-10.6.1.0.jar:lib/dom4j-1.4.jar:lib/ejisto-agent-0.1-SNAPSHOT.jar:lib/ejisto-application-0.1-SNAPSHOT.jar:lib/ejisto-core-0.1-SNAPSHOT.jar:lib/filters-2.0.235.jar:lib/forms-1.0.5.jar:lib/geronimo-j2ee-deployment_1.1_spec-1.1.jar:lib/hamcrest-all-1.1.jar:lib/isorelax-20020414.jar:lib/javassist-3.12.1.GA.jar:lib/jaxen-1.0-FCS.jar:lib/jdom-1.0.jar:lib/lambdaj-2.3.3.jar:lib/log4j-1.2.14.jar:lib/msv-20020414.jar:lib/objenesis-1.0.jar:lib/ognl-3.0.jar:lib/relaxngDatatype-20020414.jar:lib/saxpath-1.0-FCS.jar:lib/spring-aop-3.0.5.RELEASE.jar:lib/spring-asm-3.0.5.RELEASE.jar:lib/spring-beans-3.0.5.RELEASE.jar:lib/spring-context-3.0.5.RELEASE.jar:lib/spring-core-3.0.5.RELEASE.jar:lib/spring-expression-3.0.5.RELEASE.jar:lib/spring-jdbc-3.0.5.RELEASE.jar:lib/spring-tx-3.0.5.RELEASE.jar:lib/swingx-1.6.1.jar:lib/xml-apis-1.0.b2.jar";
        String jarPath = ContainerUtils.extractAgentJar(path);
        assertNotNull(jarPath);
        assertFalse(jarPath.isEmpty());
    }
}
