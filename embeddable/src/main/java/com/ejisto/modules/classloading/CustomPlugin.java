package com.ejisto.modules.classloading;

import org.springsource.loaded.ReloadEventProcessorPlugin;

/**
 * Created by IntelliJ IDEA.
 * User: celestino
 * Date: 2/16/14
 * Time: 2:10 PM
 */
public class CustomPlugin implements ReloadEventProcessorPlugin {
    @Override
    public boolean shouldRerunStaticInitializer(String typename, Class<?> clazz, String encodedTimestamp) {
        return false;
    }

    @Override
    public void reloadEvent(String typename, Class<?> clazz, String encodedTimestamp) {
        System.out.printf("reloaded %s", typename);
    }
}
