package com.ejisto.core.jetty;

import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;

import org.eclipse.jetty.webapp.WebAppContext;

public class WebAppContextRepository {
    
    private Map<String, WebAppContext> contextMap = new TreeMap<String, WebAppContext>();
    
    public void registerWebAppContext(WebAppContext context) {
        contextMap.put(context.getContextPath(), context);
    }
    
    public void unregisterWebAppContext(WebAppContext context) {
        contextMap.remove(context.getContextPath());
    }
    
    public WebAppContext getWebAppContext(String contextPath) {
        return contextMap.get(contextPath);
    }
    
    public Collection<WebAppContext> getAllContexts() {
        return contextMap.values();
    }
    
    
    
    public boolean containsWebAppContext(String contextPath) {
        return contextMap.containsKey(contextPath);
    }
    
    
}
