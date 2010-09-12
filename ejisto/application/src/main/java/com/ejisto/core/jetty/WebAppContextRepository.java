/*******************************************************************************
 * Copyright 2010 Celestino Bellone
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

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
