package com.ejisto.core.classloading;


import org.junit.Assert;
import org.junit.Test;
import org.springsource.loaded.Plugin;

/**
 * Created by IntelliJ IDEA.
 * User: celestino
 * Date: 2/15/14
 * Time: 9:35 PM
 */
public class SpringLoadedJVMPluginTest {

    @Test
    public void testInstanceCreation() throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        final Class<?> jvmPluginClass = Class.forName("com.ejisto.core.classloading.SpringLoadedJVMPlugin", false,
                                              Thread.currentThread().getContextClassLoader());
        final Object instance = jvmPluginClass.newInstance();
        Assert.assertTrue(Plugin.class.isInstance(instance));
    }
}
