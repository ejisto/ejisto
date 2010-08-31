package com.ejisto.modules.controller.wizard.installer;

import static junit.framework.Assert.assertEquals;

import org.junit.Test;

public class ApplicationScanningControllerTest {
	
	@Test
	public void testGetContextPath() {
		ApplicationScanningController applicationScanningController = new ApplicationScanningController(null);
		assertEquals("/simpleWarProject",applicationScanningController.getContextPath("/tmp/ejisto/jetty/webapps/simpleWarProject/"));
		assertEquals("/simpleWarProject",applicationScanningController.getContextPath("c:\\Windows\\Temp\\Space dir\\ejisto\\jetty\\webapps\\simpleWarProject\\"));
		assertEquals("/simpleWarProject",applicationScanningController.getContextPath("/tmp/ejisto12 3/jetty/webapps/simpleWarProject/"));
		assertEquals("/simpleWarProject",applicationScanningController.getContextPath("/tmp/(ejisto12 3)/jetty/webapps/simpleWarProject/"));
		assertEquals("/simpleWarProject",applicationScanningController.getContextPath("/tmp/(ejisto12 3)/jetty/webapps/simpleWarProject"));
	}

}
